package server.connection;

import common.protocol.ComStream;
import server.connection.handlers.AuthHandler;
import server.connection.handlers.Dispatcher;
import server.connection.handlers.GarbageHandler;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private class AcceptanceHandler implements Runnable {
        @Override
        public void run() {
            Socket soc;
            try {
                while (!Thread.interrupted()) {
                    while (pausing) wait();
                    logger.info("Wainting...");
                    soc = server.accept();
                    logger.info("Client connected");
                    ClientWorker worker = new ClientWorker(soc, incomming);
                    exec.execute(worker);
                    unauthClients.add(worker);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error", e);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Error", e);
            } finally {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static final String SERVER_IDENTYFICATOR = "ClinetServer";
    static final int DEFAULT_IDLE_TIMEOUT = 30000;
    private final ConcurrentMap<String, ClientWorker> clients = new ConcurrentHashMap<String, ClientWorker>();
    private final BlockingQueue<ClientWorker> unauthClients = new LinkedBlockingQueue<ClientWorker>();
    private final BlockingQueue<ComStream> incomming = new LinkedBlockingQueue<ComStream>();
    private final BlockingQueue<ComStream> outcomming = new LinkedBlockingDeque<ComStream>();
    private final ExecutorService exec = Executors.newCachedThreadPool();
    private AcceptanceHandler acceptanceHandler;
    private GarbageHandler garbageHandler;
    private AuthHandler authHandler;
    private Dispatcher dispatcher;
    private ServerSocket server;
    private boolean running;
    private boolean pausing;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server(int port) throws IOException {
        server = ServerSocketFactory.getDefault().createServerSocket(port);
        acceptanceHandler = new AcceptanceHandler();
        garbageHandler = new GarbageHandler(clients, 10);
        dispatcher = new Dispatcher(incomming, clients);
        authHandler = new AuthHandler(unauthClients, clients);
    }

    public synchronized void start() {
        running = true;
        exec.execute(acceptanceHandler);
        exec.execute(authHandler);
        exec.execute(dispatcher);
        exec.execute(garbageHandler);
        logger.info("Server started");
    }

    public synchronized void pause() {
        if (!running) return;
        pausing = true;
        logger.info("S1erver paused");
    }

    public synchronized void resume() {
        if (running) {
            pausing = false;
            notifyAll();
            logger.info("Server resumed");
        }
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        exec.shutdownNow();
        try {
            server.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error", e);
        }
        logger.info("Server stopped");
    }
}
