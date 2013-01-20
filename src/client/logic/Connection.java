package client.logic;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import client.logic.events.MessageEventListener;
import common.exceptions.UnauthorizedException;
import common.protocol.ComObject;
import common.protocol.ComStream;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import common.protocol.response.Failure;
import common.protocol.response.Ok;

public class Connection {
    private static final Logger logger = Logger.getLogger(Connection.class.getName());
    private final ExecutorService exec = Executors.newFixedThreadPool(3);
    private final Socket socket;
    private final String id;
    private final BlockingQueue<ComStream> outStreams;
    private boolean connected;
    private boolean authorized;
    private final BlockingQueue<ComStream> inStreams;
    protected List<LogicEventListener> logicListeners = new LinkedList<LogicEventListener>();
    protected List<MessageEventListener> messagesListener = new LinkedList<MessageEventListener>();

    public Connection(String id) throws IOException {
        this.id = id;
        System.setProperty("javax.net.ssl.keyStore", "C:/Users/Bartek/Documents/git_repo/komunikator2/ClinetServer2");
        System.setProperty("javax.net.ssl.keyStorePassword", "admin1admin2");
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/Bartek/Documents/git_repo/komunikator2/ClinetServer2");
        System.setProperty("javax.net.ssl.trustStorePassword", "admin1admin2");
        socket  = SSLSocketFactory.getDefault().createSocket();
        outStreams = new LinkedBlockingQueue<ComStream>();
        inStreams = new LinkedBlockingQueue<ComStream>();
    }

    public void connect(InetSocketAddress address, int timeout) throws IOException {
        logger.info("Connecting...");
        socket.connect(address, timeout);

        logger.info("Connected");
        OutWorker outWorker = new OutWorker(socket.getOutputStream(), outStreams, logicListeners);
        InWorker inWorker = new InWorker(socket.getInputStream(), inStreams, logicListeners);
        logger.info("Workers connected");

        exec.execute(outWorker);
        exec.execute(inWorker);
        connected = true;
        logger.info("Connecting finished");
    }

    public synchronized void disconnect() {
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
        ComObject ob = inStreams.take().obj;
        if (ob instanceof Ok) {
            fireLogicEvent(new LogicEvent(this, LogicEvent.Type.AUTH_SUCCESS));
            exec.execute(new Dispatcher(inStreams, messagesListener));
            authorized = true;
        }
        if (ob instanceof Failure) {
            fireLogicEvent(new LogicEvent(this, LogicEvent.Type.AUTH_FAILURE));
        }
        logger.info("Authentication finished");
        return ob;
    }

    public ComObject createAccount(CreateAccount acc) throws InterruptedException {
        logger.info("Creating account...");
        ComStream stream = new ComStream(acc.username, null, acc);
        outStreams.add(stream);
        ComObject ob = inStreams.take().obj;
        if(ob instanceof Ok) {
            fireLogicEvent(new LogicEvent(this, LogicEvent.Type.ACCOUNT_CREATED));
        }
        if(ob instanceof Failure) {
            fireLogicEvent(new LogicEvent(this, LogicEvent.Type.ACCOUNT_EXIST));
        }
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

    public synchronized void addLogicEventListener(LogicEventListener lst) {
        logicListeners.add(lst);
    }

    public synchronized void addMessageEventListener(MessageEventListener lst) {
        messagesListener.add(lst);
    }

    protected synchronized void fireLogicEvent(LogicEvent e) {
        for(LogicEventListener l : logicListeners) {
            l.onLogicEvent(e);
        }
    }
}
