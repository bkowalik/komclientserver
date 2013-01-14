package client.logic;


import common.exceptions.UnauthorizedException;
import common.protocol.ComObject;
import common.protocol.ComStream;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import common.protocol.response.Ok;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Connection {
    private static final Logger logger = Logger.getLogger(Connection.class.getName());
    private final ExecutorService exec = Executors.newFixedThreadPool(2);
    private final Socket socket = new Socket();
    private final String id;
    private final BlockingQueue<ComStream> outStreams;
    private boolean connected;
    private boolean authorized;
    private final Queue<ComStream> inStreams;

    public Connection(String id) {
        this.id = id;
        outStreams = new LinkedBlockingQueue<ComStream>();
        inStreams = new ConcurrentLinkedQueue<ComStream>();
    }

    public void connect(InetSocketAddress address, int timeout) throws IOException {
        logger.info("Connecting...");
        socket.connect(address, timeout);

        logger.info("Connected");
        OutWorker outWorker = new OutWorker(socket.getOutputStream(), outStreams);
        InWorker inWorker = new InWorker(socket.getInputStream(), inStreams);
        logger.info("Workers connected");

        exec.execute(outWorker);
        exec.execute(inWorker);
        connected = true;
        logger.info("Connecting finished");
    }

    public void disconnect() {
        exec.shutdownNow();
        try {
            socket.close();
        } catch (IOException e) {
        }
        connected = false;
    }

    public ComObject authenticate(Login login) throws InterruptedException {
        logger.info("Authenticating...");
        if(!connected) {
            logger.warning("Client not connected");
            return null;
        }
        ComStream stream = new ComStream(login.username, null, login);
        outStreams.add(stream);
        while (inStreams.isEmpty()) ;
        ComObject ob = inStreams.poll().obj;
        if (ob instanceof Ok) authorized = true;
        logger.info("Authentication finished");
        return ob;
    }

    public ComObject createAccount(CreateAccount acc) throws IOException {
        logger.info("Creating account...");
        ComStream stream = new ComStream(acc.username, null, acc);
        outStreams.add(stream);
        while(inStreams.isEmpty()) ;
        ComObject ob = inStreams.poll().obj;
        logger.info("Creating account finished");
        return ob;
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public Queue<ComStream> getInStreams() throws UnauthorizedException {
        if (!authorized) throw new UnauthorizedException();
        return inStreams;
    }

    public void sendStream(ComStream stream) throws UnauthorizedException {
        if (!authorized) throw new UnauthorizedException();
        outStreams.add(stream);
    }
}
