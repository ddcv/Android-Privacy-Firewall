package edu.cmu.privacy.privacyfirewall;

import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Billdqu on 11/17/16.
 */

public class TrafficOutRunnable implements Runnable {

    private Selector selector;
    private BlockingQueue<IPPacket> outputPacketsQueue;

    public TrafficOutRunnable() {

    }

    @Override
    public void run() {

    }
}
