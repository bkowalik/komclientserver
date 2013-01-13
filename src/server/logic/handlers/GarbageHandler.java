package server.logic.handlers;


import server.logic.ClientWorker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GarbageHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(GarbageHandler.class.getName());
    private final ConcurrentMap<String, ClientWorker> workers;
    private final int restTime;

    public GarbageHandler(ConcurrentMap<String, ClientWorker> workers, int restTime) {
        this.workers = workers;
        this.restTime = restTime;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            for(ConcurrentMap.Entry<String, ClientWorker> w : workers.entrySet()) {
                if(!w.getValue().isActive()) workers.remove(w.getKey());
            }
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "GarbageHandler interrupted", e);
        }
    }
}
