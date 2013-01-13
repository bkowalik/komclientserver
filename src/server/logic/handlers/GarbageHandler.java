package server.logic.handlers;


import org.apache.log4j.Logger;
import server.logic.ClientWorker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class GarbageHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(GarbageHandler.class);
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
            logger.debug("Error", e);
        }
    }
}
