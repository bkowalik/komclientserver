package server.connection.handlers;


import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.ServerLogger;
import server.connection.ClientWorker;

public class GarbageHandler implements Runnable {
    private static final Logger logger = ServerLogger.getLogger();
    private final ConcurrentMap<String, ClientWorker> workers;
    private final int restTime;

    public GarbageHandler(ConcurrentMap<String, ClientWorker> workers, int restTime) {
        this.workers = workers;
        this.restTime = restTime;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            for (ConcurrentMap.Entry<String, ClientWorker> w : workers.entrySet()) {
                if (!w.getValue().isActive()) {
                    workers.remove(w.getKey());
                    logger.info(w.getValue().getHostAddress() + ": Client garbage collected ");
                }
            }
        }
        try {
            TimeUnit.SECONDS.sleep(restTime);
        } catch (InterruptedException e) {
            logger.log(Level.FINEST, "GarbageHandler interrupted", e);
        }
    }
}
