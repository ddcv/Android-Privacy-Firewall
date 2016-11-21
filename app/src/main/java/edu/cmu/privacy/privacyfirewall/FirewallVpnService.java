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
import java.util.concurrent.ConcurrentLinkedQueue;
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

    private ConcurrentLinkedQueue<IPPacket> deviceToNetworkUDPQueue;
    private ConcurrentLinkedQueue<IPPacket> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    private Selector udpSelector;
    private Selector tcpSelector;

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
            udpSelector = Selector.open();
            tcpSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Selector Creation Error!!");
            return;
        }

        /* Init BlockingQueue */
        deviceToNetworkUDPQueue = new ConcurrentLinkedQueue<>();
        deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<>();
        networkToDeviceQueue = new ConcurrentLinkedQueue<>();

        /* Create thread pool */
        executorService = Executors.newFixedThreadPool(5);
        // start VPN Thread Runnable
        executorService.submit(new UDPInput(networkToDeviceQueue, udpSelector));
        executorService.submit(new UDPOutput(deviceToNetworkUDPQueue, udpSelector, this));
        executorService.submit(new TCPInput(networkToDeviceQueue, tcpSelector));
        executorService.submit(new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, this));
        executorService.submit(new VPNThreadRunnable(vpnInterface.getFileDescriptor(),
                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));
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

        ByteBufferPool.clear();

        try {
            vpnInterface.close();
            udpSelector.close();
            tcpSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class VPNThreadRunnable implements Runnable {

        private static final String TAG = VPNThreadRunnable.class.getSimpleName();
        private FileDescriptor vpnFileDescriptor;

        private ConcurrentLinkedQueue<IPPacket> deviceToNetworkUDPQueue;
        private ConcurrentLinkedQueue<IPPacket> deviceToNetworkTCPQueue;
        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

        public VPNThreadRunnable(FileDescriptor vpnFileDescriptor,
                           ConcurrentLinkedQueue<IPPacket> deviceToNetworkUDPQueue,
                           ConcurrentLinkedQueue<IPPacket> deviceToNetworkTCPQueue,
                           ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue)
        {
            this.vpnFileDescriptor = vpnFileDescriptor;
            this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
            this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
            this.networkToDeviceQueue = networkToDeviceQueue;
        }

        @Override
        public void run() {

            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

            try {

                Log.d(TAG, "run: ready to run");

                ByteBuffer bufferToNetwork = null;
                boolean dataSent = true;
                boolean dataReceived;

                // Use a loop to pass packets.
                while (true) {
                    if (dataSent)
                        bufferToNetwork = ByteBufferPool.acquire();
                    else
                        bufferToNetwork.clear();

                    int readBytes = vpnInput.read(bufferToNetwork);
                    if (readBytes > 0)
                    {
                        dataSent = true;
                        bufferToNetwork.flip();
                        IPPacket packet = new IPPacket(bufferToNetwork);

                        // do the filtering
                        packet = Monitor.filter(packet);

                        // if packet == null for any reason(mostly filtered by our rule), continue
                        if (packet != null) {
                            if (packet.isUDP()) {
                                deviceToNetworkUDPQueue.offer(packet);
                            } else if (packet.isTCP()) {
                                deviceToNetworkTCPQueue.offer(packet);
                            } else {
                                Log.w(TAG, "Unknown packet type");
                                Log.w(TAG, packet.ip4Header.toString());
                                dataSent = false;
                            }
                        }
                    }
                    else
                    {
                        dataSent = false;
                    }

                    ByteBuffer bufferFromNetwork = networkToDeviceQueue.poll();
                    if (bufferFromNetwork != null)
                    {
                        bufferFromNetwork.flip();
                        while (bufferFromNetwork.hasRemaining())
                            vpnOutput.write(bufferFromNetwork);
                        dataReceived = true;

                        ByteBufferPool.release(bufferFromNetwork);
                    }
                    else
                    {
                        dataReceived = false;
                    }

                    if (!dataSent && !dataReceived)
                        Thread.sleep(10);
                }
            } catch (IOException e) {
                Log.w(TAG, e.toString(), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    vpnInput.close();
                    vpnOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
