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

public class TCPTrafficOutRunnable implements Runnable {

    private Selector selector;
    private BlockingQueue<IPPacket> TCPPacketsOutputQueue;
    private FirewallVpnService firewallVpnService;

    public TCPTrafficOutRunnable(BlockingQueue<IPPacket> bq, Selector s, FirewallVpnService vpn) {
        this.TCPPacketsOutputQueue = bq;
        this.selector = s;
        this.firewallVpnService = vpn;
    }

    @Override
    public void run() {
        try {
            while (true) {
                IPPacket packet = TCPPacketsOutputQueue.take();

                InetAddress destinationAddress = packet.ip4Header.destinationAddress;


            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
