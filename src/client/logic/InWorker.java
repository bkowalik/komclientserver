package client.logic;


import common.protocol.ComStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Queue;

public class InWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(InWorker.class);
    private final Queue<ComStream> inStreams;
    private final ObjectInput input;

    public InWorker(InputStream in, Queue<ComStream> inStreams) throws IOException {
        if(in == null) throw new NullPointerException("InputStream is null");
        input = new ObjectInputStream(in);
        this.inStreams = inStreams;
    }

    @Override
    public void run() {
        ComStream stream;
        try {
            while(!Thread.interrupted()) {
                try {
                    stream = (ComStream)input.readObject();
                    logger.debug(stream.to + " odebrałem");
                    inStreams.add(stream);
                } catch (ClassNotFoundException e) {
                    logger.fatal("Error", e);
                    break;
                } catch (IOException e) {
                    logger.fatal("Error", e);
                    break;
                }
            }
        } finally {
            try { input.close(); } catch (IOException e) { }
        }
    }
}
