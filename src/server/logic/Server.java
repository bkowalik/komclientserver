package server.logic;

import common.protocol.ComStream;
import org.apache.log4j.Logger;

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
                while(!Thread.interrupted()) {
                    //while(pausing) wait();
                    logger.debug("Wainting...");
                    soc = server.accept();
                    logger.debug("Client connected");
                    ClientWorker worker = new ClientWorker(soc, clinetAuthenticator);
                    logger.debug("Client thread created");
                    exec.execute(worker);
                    logger.debug("Client thread started");
                }
            } catch (IOException e) {
                logger.warn("Error", e);
            }/* catch (InterruptedException e) {
                e.printStackTrace();
            }*/ finally {
                try { server.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    static final String SERVER_IDENTYFICATOR = "ClinetServer";
    static final int DEFAULT_IDLE_TIMEOUT = 30000;
    private ConcurrentMap<String, ClientWorker> clients = new ConcurrentHashMap<String, ClientWorker>();
    private BlockingQueue<ComStream> incomming = new LinkedBlockingQueue<ComStream>();
    private ExecutorService exec = Executors.newCachedThreadPool();
    private int port;
    private Authenticator clinetAuthenticator = new ClinetAuthenticator();
    private AcceptanceHandler acceptanceHandler;
    private ServerSocket server;
    private boolean running;
    private boolean pausing;
    private static final Logger logger = Logger.getLogger(Server.class);

    public Server(int port) throws IOException {
        this.port = port;
        server = new ServerSocket(port);
        acceptanceHandler = new AcceptanceHandler();
    }

    public synchronized void start() {
        running = true;
        exec.execute(acceptanceHandler);
        logger.debug("Server started");
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
        try {
            server.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
