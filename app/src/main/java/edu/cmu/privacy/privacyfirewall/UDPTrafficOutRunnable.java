package edu.cmu.privacy.privacyfirewall;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Billdqu on 11/17/16.
 */

public class UDPTrafficOutRunnable implements Runnable {

    private static final String TAG = "UDPOUT";

    private Selector selector;
    private BlockingQueue<IPPacket> UDPPacketsOutputQueue;
    private FirewallVpnService firewallVpnService;

    public UDPTrafficOutRunnable(BlockingQueue<IPPacket> bq, Selector s, FirewallVpnService vpn) {
        this.UDPPacketsOutputQueue = bq;
        this.selector = s;
        this.firewallVpnService = vpn;
    }

    @Override
    public void run() {
        try {
            while (true) {
                IPPacket packet = UDPPacketsOutputQueue.take();

                InetAddress destinationAddress = packet.ip4Header.destinationAddress;
                int destinationPort = packet.udpHeader.destinationPort;
                int sourcePort = packet.udpHeader.sourcePort;

                /* The UDP channel can be used to pass/get ip package to/from server */
                DatagramChannel outputChannel = DatagramChannel.open();
                /* Connect to localhost */
                outputChannel.connect(new InetSocketAddress(destinationAddress, destinationPort));
                outputChannel.configureBlocking(false);

                packet.swapSourceAndDestination();

                Log.d(TAG, packet.ip4Header.sourceAddress + ":" + packet.udpHeader.sourcePort
                        + "  -->  " + packet.ip4Header.destinationAddress + ":" + packet.udpHeader.destinationPort);

                selector.wakeup();
                outputChannel.register(selector, SelectionKey.OP_READ, packet);

                Log.d(TAG, "registered");

                firewallVpnService.protect(outputChannel.socket());

                ByteBuffer payloadBuffer = packet.contentBuffer;
                while (payloadBuffer.hasRemaining()) {
                    outputChannel.write(payloadBuffer);
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
