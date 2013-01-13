package server.logic.handlers;


import org.apache.log4j.Logger;
import server.logic.ClientWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class AuthHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(AuthHandler.class);
    private final BlockingQueue<ClientWorker> unauthorized;
    private final ConcurrentMap<String, ClientWorker> authorized;

    public AuthHandler(BlockingQueue<ClientWorker> unauth, ConcurrentMap<String, ClientWorker> auth) {
        if(unauth == null) throw new NullPointerException("Unauth clients queue cannot be null");
        if(auth == null) throw new NullPointerException("Autorized clients map cannot be null");

        unauthorized = unauth;
        authorized = auth;
    }

    @Override
    public void run() {
        ClientWorker worker;
        logger.debug("AuthHandler started");
        while(!Thread.interrupted()) {
            try {
                worker = unauthorized.take();
            } catch (InterruptedException e) {
                logger.debug("Error", e);
                break;
            }

            if(!worker.isActive()) {
                continue;
            }

            if(!worker.isAuthenticated()) {
                unauthorized.add(worker);
                continue;
            }

            logger.debug("New authorized user " + worker.getId());
            authorized.put(worker.getId(), worker);
        }
    }
}
