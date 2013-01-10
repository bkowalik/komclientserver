package server.logic;

import common.protocol.ComStream;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {
    private class AcceptanceHandler implements Runnable {
        @Override
        public void run() {
            Socket soc;
            try {
                while(Thread.interrupted()) {
                    while(pausing) wait();
                    soc = server.accept();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try { server.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    static final int DEFAULT_IDLE_TIMEOUT = 100;
    private ConcurrentMap<String, ClientWorker> clients = new ConcurrentHashMap<String, ClientWorker>();
    private BlockingQueue<ComStream> incomming = new LinkedBlockingQueue<ComStream>();
    private ExecutorService exec = Executors.newCachedThreadPool();
    private int port;
    private AcceptanceHandler acceptanceHandler;
    private ServerSocket server;
    private boolean running;
    private boolean pausing;

    public Server(int port) throws IOException {
        this.port = port;
        server = ServerSocketFactory.getDefault().createServerSocket(port);
        acceptanceHandler = new AcceptanceHandler();
    }

    public synchronized void start() {
        running = true;
    }

    public synchronized void pause() {
        pausing = true;
    }

    public synchronized void resume() {
        pausing = false;
        notifyAll();
    }

    public synchronized void stop() {
        running = false;
        exec.shutdownNow();
    }
}
