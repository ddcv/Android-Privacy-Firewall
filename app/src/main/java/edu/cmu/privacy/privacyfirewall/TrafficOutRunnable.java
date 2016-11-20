package edu.cmu.privacy.privacyfirewall;

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

public class TrafficOutRunnable implements Runnable {

    private Selector selector;
    private BlockingQueue<IPPacket> outputPacketsQueue;
    private FirewallVpnService firewallVpnService;

    public TrafficOutRunnable(BlockingQueue<IPPacket> bq, Selector s, FirewallVpnService vpn) {
        this.outputPacketsQueue = bq;
        this.selector = s;
        this.firewallVpnService = vpn;
    }

    @Override
    public void run() {
        try {
            while (true) {
                IPPacket packet = outputPacketsQueue.take();

                InetAddress destinationAddress = packet.ip4Header.destinationAddress;
                int destinationPort = packet.udpHeader.destinationPort;
                int sourcePort = packet.udpHeader.sourcePort;

                /* The UDP channel can be used to pass/get ip package to/from server */
                DatagramChannel outputChannel = DatagramChannel.open();
                /* Connect to localhost */
                outputChannel.connect(new InetSocketAddress(destinationAddress, destinationPort));
                outputChannel.configureBlocking(false);

                //packet.swapSourceAndDestination();

                selector.wakeup();
                outputChannel.register(selector, SelectionKey.OP_READ, packet);

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
