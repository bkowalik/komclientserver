package server.connection.handlers;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import server.ServerLogger;
import server.connection.ClientWorker;

public class AuthHandler implements Runnable {
    private static final Logger logger = ServerLogger.getLogger();
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
                break;
            }

            if (!worker.isActive()) {
                worker.shutDownNow();
                continue;
            }

            if (!worker.isAuthenticated()) {
                unauthorized.add(worker);
                continue;
            }

            logger.info(worker.getHostAddress() + ": AuthHandler - authenticated.");
            authorized.put(worker.getId(), worker);
        }
    }
}
