package server.connection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.protocol.ComStream;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import common.protocol.response.Failure;
import common.protocol.response.Ok;
import server.ServerLogger;

public class ClientWorker implements Runnable {
    private static final Logger logger = ServerLogger.getLogger();
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private String id;
    private boolean running;
    private boolean authenticated;
    private int attempts = 1;
    private final BlockingQueue<ComStream> incomming;
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private final String hostAddress;


    public ClientWorker(Socket socket, BlockingQueue<ComStream> incomming) throws IOException {
        if (socket == null) throw new NullPointerException("Socket cannot be null");
        if (incomming == null) throw new NullPointerException("Incomming messages queue cannot be null");

        this.socket = socket;
        hostAddress = socket.getInetAddress().getHostAddress();
        this.incomming = incomming;

        this.output = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.output.flush();
        this.input = new ObjectInputStream(this.socket.getInputStream());

        running = true;
    }


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().interrupted() && !authenticated) {
                try {
                    ComStream stream = (ComStream) input.readObject();
                    if(stream.obj instanceof Login) {
                        logger.info(hostAddress + ": login attempt");
                        if(authenticate(stream)) {
                            break;
                        } else {
                            if(attempts > MAX_LOGIN_ATTEMPTS) {
                                logger.info(hostAddress + " max attempts exceeded");
                                shutDownNow();
                                break;
                            } else {
                                attempts++;
                                continue;
                            }
                        }
                    } else if(stream.obj instanceof CreateAccount) {
                        logger.info(hostAddress + ": account creation attempt");
                        CreateAccount abc = (CreateAccount)stream.obj;
                        if(abc.username.equals("nowy") && abc.password.equals("haselko")) {
                            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, abc.username, new Ok(Ok.Type.ACCOUNT_CREATED)));
                            output.flush();
                            output.reset();
                            logger.config(hostAddress + ": Account created");
                        } else {
                            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, abc.username, new Failure(Failure.Types.LOGIN_EXISTS)));
                            output.flush();
                            output.reset();
                            logger.config(hostAddress + ": Account creation failure");
                        }
                    } else {
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR,
                                null,
                                new Failure(Failure.Types.HACK_ATTEMPT)
                        ));
                        output.flush();
                        output.reset();
                        logger.warning(hostAddress + ": Hack attempt");
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.FINEST, hostAddress + ": Failure", e);
                    break;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.FINEST, hostAddress + ": Failure", e);
                    break;
                }
            }

            ComStream stream;
            Object obj;
            while (!Thread.currentThread().interrupted() && authenticated) {
                try {
                    obj = null;
                    obj = input.readObject();
                    if (obj != null) {
                        if (!(obj instanceof ComStream)) continue;
                        stream = (ComStream) obj;
                        incomming.add(stream);
                    }
                } catch (IOException e) {
                    logger.log(Level.FINEST, hostAddress + ": Failure", e);
                    return;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.FINEST, hostAddress + ": Failure", e);
                    return;
                }
            }
        } finally {
            shutDownNow();
        }
    }

    public synchronized void shutDownNow() {
        running = false;
        authenticated = false;
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            logger.log(Level.FINEST, hostAddress + ": Very bad!", e);
        } finally {
            logger.info(hostAddress + ": disconnected");
        }
    }

    protected boolean authenticate(ComStream stream) throws IOException {
        Login login = (Login) stream.obj;
        if ((login.username.equals("bartek") && login.password.equals("haslo"))
                || (login.username.equals("misia") && login.password.equals("maslo"))) {
            authenticated = true;
            id = login.username;
            Ok ok = new Ok(Ok.Type.AUTHENTICATED);
            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, login.username, ok));
            output.flush();
            output.reset();
            logger.info(hostAddress + ": authentication success.");
            return true;
        } else {
            Failure e = new Failure(Failure.Types.NOT_AUTHORIZED);
            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, login.username, e));
            output.flush();
            output.reset();
            logger.info(hostAddress + ": authentication failure.");
            return false;
        }
    }

    public synchronized boolean isActive() {
        return running;
    }

    public synchronized boolean isAuthenticated() {
        return authenticated;
    }

    public synchronized void sendResponse(ComStream comObj) throws IOException {
        output.writeObject(comObj);
        output.flush();
        output.reset();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getHostAddress() {
        return hostAddress;
    }
}
