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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private BlockingQueue<IPPacket> outputPacketsQueue;
    private BlockingQueue<ByteBuffer> inputPacketsQueue;
    private ExecutorService executorService;

    private Selector selector;

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

        /* Selector */
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Selector Creation Error!!");
            return;
        }

        /* Create thread pool */
        executorService = Executors.newFixedThreadPool(3);
        // start VPN Thread Runnable
        executorService.submit(new VPNThreadRunnable(vpnInterface.getFileDescriptor()));
        executorService.submit(new TrafficInRunnable(inputPacketsQueue, selector));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: Close All Threads

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

                /* The UDP channel can be used to pass/get ip package to/from server */
                DatagramChannel tunnel = DatagramChannel.open();
                /* Connect to localhost */
                tunnel.connect(new InetSocketAddress("10.0.0.2", 8087));
                /* Protect this socket,
                   so package send by it will not be feedback to the vpn service. */
                protect(tunnel.socket());

                Log.d(TAG, "run: ready to run");
                // Use a loop to pass packets.
                while (true) {
                    ByteBuffer bufferToNetwork = ByteBuffer.allocateDirect(1 << 10);

                    //get packet with in
                    int bytes = vpnInput.read(bufferToNetwork);
//                    Log.d(TAG, "run: " + bytes);

                    bufferToNetwork.flip();
                    while (bufferToNetwork.hasRemaining()) {
                        vpnOutput.write(bufferToNetwork);
                    }

                    if (bytes > 0) {
                        bufferToNetwork.flip();
                        IPPacket packet = new IPPacket(bufferToNetwork);

                        // do the filtering
                        packet = Monitor.filter(packet);

                        // if packet == null for any reason(mostly filtered by our rule), continue
                        if (packet == null) continue;

                        outputPacketsQueue.offer(packet);

                        Log.d(TAG, "run: " + packet);

                    }
//                    bufferToNetwork.flip();
////                    tunnel.write(bufferToNetwork);
//                    while (bufferToNetwork.hasRemaining()) {
//                        vpnOutput.write(bufferToNetwork);
//                    }

                    //put packet to tunnel
//                    tunnel.write(bufferToNetwork);

                    //get packet form tunnel
                    //return packet with out
                    //sleep is a must
                    Thread.sleep(100);
                }

//                while (!Thread.interrupted()) {
//
//                    /* Read the bytes from inner network to buffer */
//                    ByteBuffer bufferToNetwork = null;
//
//                    // TODO: acquire buffer
////                    bufferToNetwork;
//
//                    int readBytes = vpnInput.read(bufferToNetwork);
//                    if (readBytes > 0) {
//                        /* Send the read bytes to outer network */
//                        bufferToNetwork.flip();
//                        // TODO: Send
//                    }
//                    else {
//
//                    }
//
//
//                }

            } catch (IOException e) {
                Log.w(TAG, e.toString(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
