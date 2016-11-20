package edu.cmu.privacy.privacyfirewall;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Billdqu on 9/30/16.
 */

public class FirewallVpnService extends VpnService {

    /* VPN Configures */
    private static final String TAG = "VPNService";
    public static final String VPN_ADDRESS = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private BlockingQueue<IPPacket> outputUDPPacketsQueue;
    private BlockingQueue<IPPacket> outputTCPPacketsQueue;
    private BlockingQueue<ByteBuffer> inputPacketsQueue;
    private ExecutorService executorService;

    private Selector UDPSelector;
    private Selector TCPSelector;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Configure VPN and get the interface */
        if (vpnInterface == null) {
            Builder builder = new Builder();
            vpnInterface = builder.setSession("MyVPNService")
                    .addAddress(VPN_ADDRESS, 32)
                    .addDnsServer("8.8.8.8")
                    .addRoute(VPN_ROUTE, 0)
                    .setConfigureIntent(pendingIntent).establish();
        }

        /* Init Selector */
        try {
            UDPSelector = Selector.open();
            TCPSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Selector Creation Error!!");
            return;
        }

        /* Init BlockingQueue */
        outputUDPPacketsQueue = new LinkedBlockingQueue<>();
        outputTCPPacketsQueue = new LinkedBlockingQueue<>();
        inputPacketsQueue = new LinkedBlockingQueue<>();

        /* Create thread pool */
        executorService = Executors.newFixedThreadPool(5);
        // start VPN Thread Runnable
        executorService.submit(new VPNThreadRunnable(vpnInterface.getFileDescriptor()));
        executorService.submit(new UDPTrafficOutRunnable(outputUDPPacketsQueue, UDPSelector, this));
        executorService.submit(new UDPTrafficInRunnable(inputPacketsQueue, UDPSelector));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: Close All Threads

        executorService.shutdown();

        try {
            UDPSelector.close();
            TCPSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class VPNThreadRunnable implements Runnable {

        private FileDescriptor vpnFileDescriptor;

        public VPNThreadRunnable(FileDescriptor vpnFileDescriptor) {
            this.vpnFileDescriptor = vpnFileDescriptor;
        }

        @Override
        public void run() {

            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

            try {

                Log.d(TAG, "run: ready to run");
                // Use a loop to pass packets.
                while (true) {
                    ByteBuffer bufferToNetwork = ByteBuffer.allocateDirect(1 << 10);

                    //get packet with in
                    int bytes = vpnInput.read(bufferToNetwork);

                    if (bytes > 0) {
                        bufferToNetwork.flip();
                        IPPacket packet = new IPPacket(bufferToNetwork);

                        // do the filtering
                        packet = Monitor.filter(packet);

                        // if packet == null for any reason(mostly filtered by our rule), continue
                        if (packet == null) continue;

                        if (packet.isTCP()) {
                            outputTCPPacketsQueue.offer(packet);
                        } else if (packet.isUDP()) {
                            outputUDPPacketsQueue.offer(packet);
                        } else {
                            // TODO: Exception
                        }

                        Log.d(TAG, "run: " + packet);

                    }

                    ByteBuffer bufferFromNetwork = inputPacketsQueue.poll();
                    if (bufferFromNetwork != null)
                    {
                        bufferFromNetwork.flip();
                        while (bufferFromNetwork.hasRemaining())
                            vpnOutput.write(bufferFromNetwork);
                    }

                    //sleep is a must
                    Thread.sleep(10);
                }
            } catch (IOException e) {
                Log.w(TAG, e.toString(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
