package client.logic;


import common.protocol.ComStream;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class OutWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(OutWorker.class);
    private final ObjectOutputStream output;
    private final BlockingQueue<ComStream> outStreams;

    public OutWorker(OutputStream out, BlockingQueue<ComStream> outStreams) throws IOException {
        if(out == null) throw new NullPointerException("OutputStream is null");
        if(outStreams == null) throw new NullPointerException("BlockingQueue is null");

        output = new ObjectOutputStream(new BufferedOutputStream(out));
        output.flush();
        this.outStreams = outStreams;
    }

    @Override
    public void run() {
        ComStream stream;
        try {
            while(!Thread.interrupted()) {
                try {
                    stream = outStreams.take();
                    output.writeObject(stream);
                    output.flush();
                    output.reset();
                } catch (InterruptedException e) {
                    logger.fatal("Error", e);
                    break;
                } catch (IOException e) {
                    logger.fatal("Error", e);
                    break;
                }
            }
        } finally {
            try { output.close(); } catch (IOException e) { }
        }
    }
}
