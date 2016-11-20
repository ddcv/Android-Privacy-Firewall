package edu.cmu.privacy.privacyfirewall;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Billdqu on 11/17/16.
 */

public class UDPTrafficInRunnable implements Runnable {

    private Selector selector;
    private BlockingQueue<ByteBuffer> outputPacketsQueue;

    public UDPTrafficInRunnable(BlockingQueue<ByteBuffer> queue, Selector s) {
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

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext() && !Thread.interrupted()) {
                    SelectionKey key = keyIterator.next();


                    if (key.isValid() && key.isReadable()) {
                        keyIterator.remove();

                        ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(1 << 10);
                        // Leave space for the header
//                        receiveBuffer.position(HEADER_SIZE);

                        DatagramChannel inputChannel = (DatagramChannel) key.channel();
                        int readBytes = inputChannel.read(receiveBuffer);

//                        Packet referencePacket = (Packet) key.attachment();
//                        referencePacket.updateUDPBuffer(receiveBuffer, readBytes);
//                        receiveBuffer.position(HEADER_SIZE + readBytes);

                        outputPacketsQueue.offer(receiveBuffer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
