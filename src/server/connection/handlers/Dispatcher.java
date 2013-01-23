package server.connection.handlers;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.protocol.Message;
import server.ServerLogger;
import server.connection.ClientWorker;

import common.protocol.ComStream;
import server.db.DBManager;

public class Dispatcher implements Runnable {
    private static final Logger logger = ServerLogger.getLogger();
    private final BlockingQueue<ComStream> toDispatch;
    private final ConcurrentMap<String, ClientWorker> clients;
    private final DBManager dbManager;

    public Dispatcher(DBManager dbManager, BlockingQueue<ComStream> incomming, ConcurrentMap<String, ClientWorker> clients) {
        if (incomming == null) throw new NullPointerException("Incomming queue cannot be null");
        if (clients == null) throw new NullPointerException("Clients map cannot be null");

        this.dbManager = dbManager;
        this.clients = clients;
        toDispatch = incomming;
    }

    @Override
    public void run() {
        try {
            logger.config("Dispatcher started");
            while (!Thread.interrupted()) {
                ComStream comObj = toDispatch.take();
                ClientWorker clientWorker = clients.get(comObj.to);

                if (clientWorker == null) {
                    dbManager.dbStoreMsg(comObj.from, comObj.to, ((Message)comObj.obj).body, ((Message)comObj.obj).date.getTime());
                    continue;
                }

                try {
                    clientWorker.sendResponse(comObj);
                } catch (IOException e) {
                    logger.log(Level.FINEST, "Failure", e);
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.FINEST, "Dispatcher interrupted", e);
        }
    }
}
