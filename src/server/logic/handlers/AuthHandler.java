package server.logic.handlers;


import server.logic.ClientWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class AuthHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(AuthHandler.class.getName());
    private final BlockingQueue<ClientWorker> unauthorized;
    private final ConcurrentMap<String, ClientWorker> authorized;

    public AuthHandler(BlockingQueue<ClientWorker> unauth, ConcurrentMap<String, ClientWorker> auth) {
        if (unauth == null) throw new NullPointerException("Unauth clients queue cannot be null");
        if (auth == null) throw new NullPointerException("Autorized clients map cannot be null");

        unauthorized = unauth;
        authorized = auth;
    }

    @Override
    public void run() {
        ClientWorker worker;
        logger.config("AuthHandler started");
        while (!Thread.interrupted()) {
            try {
                worker = unauthorized.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            if (!worker.isActive()) {
                continue;
            }

            if (!worker.isAuthenticated()) {
                unauthorized.add(worker);
                continue;
            }

            logger.info("Client " + worker.getId() + " authenticated.");
            authorized.put(worker.getId(), worker);
        }
    }
}
