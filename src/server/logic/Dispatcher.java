package server.logic;

import common.protocol.ComStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

public class Dispatcher implements Runnable {
    private final BlockingQueue<ComStream> toDispatch;
    private final ConcurrentMap<String, ClientWorker> clients;

    public Dispatcher(BlockingQueue<ComStream> incomming, ConcurrentMap<String, ClientWorker> clients) {
        this.clients = clients;
        toDispatch = incomming;
    }

    @Override
    public void run() {
        try {
            while(!Thread.interrupted()) {
                ComStream comObj = toDispatch.take();
                ClientWorker clientWorker = clients.get(comObj.to);

                if(clientWorker == null) {
                    //TODO: dodawanie do bazy danych
                    continue;
                }

                clientWorker.sendResponse(comObj);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
