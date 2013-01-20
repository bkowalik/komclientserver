package client.logic;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import client.logic.events.MessageEvent;
import client.logic.events.MessageEventListener;
import common.protocol.ComStream;
import common.protocol.Message;

public class InWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(InWorker.class.getName());
    private final Queue<ComStream> inStreams;
    private final ObjectInput input;
    private final List<LogicEventListener> logicListeners;
    private final List<MessageEventListener> messageListeners;

    public InWorker(InputStream in, Queue<ComStream> inStreams, List<LogicEventListener> logicListeners, List<MessageEventListener> messageListeners) throws IOException {
        if (in == null) throw new NullPointerException("InputStream is null");
        input = new ObjectInputStream(in);
        this.inStreams = inStreams;
        this.logicListeners = logicListeners;
        this.messageListeners = messageListeners;
    }

    @Override
    public void run() {
        ComStream stream;
        try {
            while (!Thread.interrupted()) {
                try {
                    stream = (ComStream) input.readObject();
                    inStreams.add(stream);
                } catch (ClassNotFoundException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                    fireLogicEvent(new LogicEvent(this));
                    break;
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failure", e);
                    fireLogicEvent(new LogicEvent(this));
                    break;
                }
            }
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failure", e);
                fireLogicEvent(new LogicEvent(this));
            }
        }
    }

    private synchronized void fireLogicEvent(LogicEvent e) {
        for(LogicEventListener l : logicListeners) {
            l.onLogicEvent(e);
        }
    }

    private synchronized void fireMessageEvent(MessageEvent e) {
        for(MessageEventListener l : messageListeners) {
            l.onMessageIncomming(e);
        }
    }
}
