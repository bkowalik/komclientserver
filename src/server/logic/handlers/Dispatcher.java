package server.logic.handlers;

import common.protocol.ComStream;
import server.logic.ClientWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(Dispatcher.class.getName());
    private final BlockingQueue<ComStream> toDispatch;
    private final ConcurrentMap<String, ClientWorker> clients;

    public Dispatcher(BlockingQueue<ComStream> incomming, ConcurrentMap<String, ClientWorker> clients) {
        if(incomming == null) throw new NullPointerException("Incomming queue cannot be null");
        if(clients == null) throw new NullPointerException("Clients map cannot be null");

        this.clients = clients;
        toDispatch = incomming;
    }

    @Override
    public void run() {
        try {
            logger.config("Dispatcher started");
            while(!Thread.interrupted()) {
                ComStream comObj = toDispatch.take();
                ClientWorker clientWorker = clients.get(comObj.to);

                if(clientWorker == null) {
                    //TODO: dodawanie do bazy danych
                    continue;
                }
                clientWorker.toSend.put(comObj);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, "Dispatcher interrupted", e);
        }
    }
}
