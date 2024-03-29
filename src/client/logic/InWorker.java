package client.logic;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.ClientLogger;
import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import common.protocol.ComStream;

import static client.ClientLogger.*;

public class InWorker implements Runnable {
    private static final Logger logger = getLogger();
    private final Queue<ComStream> inStreams;
    private final ObjectInput input;
    private final List<LogicEventListener> logicListeners;

    public InWorker(InputStream in, Queue<ComStream> inStreams, List<LogicEventListener> logicListeners) throws IOException {
        if (in == null) throw new NullPointerException("InputStream is null");
        input = new ObjectInputStream(in);
        this.inStreams = inStreams;
        this.logicListeners = logicListeners;
    }

    @Override
    public void run() {
        ComStream stream;
        try {
            while (!Thread.interrupted()) {
                try {
                    stream = (ComStream) input.readObject();
                    inStreams.add(stream);
                    logger.info("ComStream incomming");
                } catch (ClassNotFoundException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                    break;
                } catch (IOException e) {
                    logger.log(Level.FINEST, "Failure", e);
                    fireLogicEvent(new LogicEvent(this, LogicEvent.Type.DISCONNECT));
                    break;
                }
            }
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.log(Level.FINEST, "Failure", e);
            }
        }
    }

    private synchronized void fireLogicEvent(LogicEvent e) {
        for(LogicEventListener l : logicListeners) {
            l.onLogicEvent(e);
        }
    }
}
