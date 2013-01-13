package server.logic.handlers;

import common.protocol.ComStream;
import org.apache.log4j.Logger;
import server.logic.ClientWorker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class Dispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(Dispatcher.class);
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
            logger.debug("Dispatcher started");
            while(!Thread.interrupted()) {
                logger.debug("Taking message...");
                ComStream comObj = toDispatch.take();
                ClientWorker clientWorker = clients.get(comObj.to);
                System.out.println("Dispatcher: Idzie wiadomość do " + comObj.to);

                if(clientWorker == null) {
                    //TODO: dodawanie do bazy danych
                    continue;
                }
                clientWorker.toSend.put(comObj);
                System.out.println("Dispatcher: Posłałem wiadomosć z " + comObj.from + " do " + clientWorker.getId());
                System.out.println("Dispatcher: " + clientWorker.toSend.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
