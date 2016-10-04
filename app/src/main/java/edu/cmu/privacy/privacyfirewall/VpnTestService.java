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

/**
 * Created by Billdqu on 9/30/16.
 */

public class VpnTestService extends VpnService {

    private static final String TAG = "VPNService";
    private static final String VPN_ADDRESS = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private Thread mThread;

    @Override
    public void onCreate() {
        super.onCreate();

        /* Configure VPN and get the interface */
        if (vpnInterface == null) {
            Builder builder = new Builder();
            vpnInterface = builder.setSession("MyTestVPNService")
                    .addAddress(VPN_ADDRESS, 32)
                    .addDnsServer("8.8.8.8")
                    .addRoute(VPN_ROUTE, 0)
                    .setConfigureIntent(pendingIntent).establish();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mThread = new Thread(new VPNThreadFunction(vpnInterface.getFileDescriptor()));

        mThread.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: Close All Threads

    }


    private class VPNThreadFunction implements Runnable {

        private FileDescriptor vpnFileDescriptor;

        public VPNThreadFunction(FileDescriptor vpnFileDescriptor) {
            this.vpnFileDescriptor = vpnFileDescriptor;


        }

        @Override
        public void run() {

            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

            try {
                ByteBuffer bufferToNetwork = ByteBuffer.allocateDirect(1 << 14);


                /* The UDP channel can be used to pass/get ip package to/from server */
                DatagramChannel tunnel = DatagramChannel.open();
                /* Connect to localhost */
                tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                /* Protect this socket,
                   so package send by it will not be feedback to the vpn service. */
                protect(tunnel.socket());

                // Use a loop to pass packets.
                while (true) {
                    //get packet with in
                    vpnInput.read(bufferToNetwork);

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
