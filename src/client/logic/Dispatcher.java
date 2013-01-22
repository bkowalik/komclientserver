package client.logic;

import client.ClientLogger;
import client.logic.events.MessageEvent;
import client.logic.events.MessageEventListener;
import common.protocol.ComStream;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static client.ClientLogger.*;

public class Dispatcher implements Runnable {
    private static final Logger logger = getLogger();
    private final BlockingQueue<ComStream> incomming;
    private final List<MessageEventListener> mlst;

    public Dispatcher(BlockingQueue<ComStream> incomming, List<MessageEventListener> mlst) {
        this.incomming = incomming;
        this.mlst = mlst;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                ComStream cs = incomming.take();
                fireMessageEvent(new MessageEvent(this, cs));
            } catch (InterruptedException e) {
                logger.log(Level.CONFIG, "Error", e);
            }
        }
    }

    private synchronized void fireMessageEvent(MessageEvent e) {
        for(MessageEventListener l : mlst) {
            l.onMessageIncomming(e);
        }
    }
}
