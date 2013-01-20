package client.logic;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import common.protocol.ComStream;

public class OutWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(OutWorker.class.getName());
    private final ObjectOutputStream output;
    private final BlockingQueue<ComStream> outStreams;
    private final List<LogicEventListener> logicListeners;

    public OutWorker(OutputStream out, BlockingQueue<ComStream> outStreams, List<LogicEventListener> logicListeners) throws IOException {
        if (out == null) throw new NullPointerException("OutputStream is null");
        if (outStreams == null) throw new NullPointerException("BlockingQueue is null");

        output = new ObjectOutputStream(new BufferedOutputStream(out));
        output.flush();
        this.outStreams = outStreams;
        this.logicListeners = logicListeners;
    }

    @Override
    public void run() {
        ComStream stream;
        try {
            while (!Thread.interrupted()) {
                try {
                    stream = outStreams.take();
                    output.writeObject(stream);
                    output.flush();
                    output.reset();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Failure", e);
                    break;
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failure", e);
                    break;
                }
            }
        } finally {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
    }

    private synchronized void fireLogicEvent(LogicEvent e) {
        for(LogicEventListener l : logicListeners) {
            l.onLogicEvent(e);
        }
    }
}
