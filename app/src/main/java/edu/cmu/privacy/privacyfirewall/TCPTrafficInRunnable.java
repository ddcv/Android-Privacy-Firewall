package edu.cmu.privacy.privacyfirewall;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Billdqu on 11/17/16.
 */

public class TCPTrafficInRunnable implements Runnable {

    private Selector selector;
    private BlockingQueue<ByteBuffer> outputPacketsQueue;

    public TCPTrafficInRunnable(BlockingQueue<ByteBuffer> queue, Selector s) {
        this.selector = s;
        this.outputPacketsQueue = queue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                    Thread.sleep(10);
                    continue;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
