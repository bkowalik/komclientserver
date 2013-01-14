package server.connection;

import common.protocol.ComStream;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import common.protocol.response.Error;
import common.protocol.response.Ok;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientWorker.class.getName());
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private String id;
    private boolean running;
    private boolean authenticated;
    private int attempts = 1;
    private final BlockingQueue<ComStream> incomming;
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    static {
        logger.setLevel(Level.ALL);
    }

    public ClientWorker(Socket socket, BlockingQueue<ComStream> incomming) throws IOException {
        if (socket == null) throw new NullPointerException("Socket cannot be null");
        if (incomming == null) throw new NullPointerException("Incomming messages queue cannot be null");

        this.socket = socket;
        this.incomming = incomming;

        this.output = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.output.flush();
        this.input = new ObjectInputStream(this.socket.getInputStream());

        running = true;
    }


    @Override
    public void run() {
        try {
            while (!Thread.interrupted() && !authenticated) {
                try {
                    ComStream stream = (ComStream) input.readObject();
                    if(stream.obj instanceof Login) {
                        logger.info("Login attempt");
                        if(authenticate(stream)) {
                            break;
                        } else {
                            if(attempts > MAX_LOGIN_ATTEMPTS) {
                                logger.info("Max attempts exceeded");
                                shutDownNow();
                                break;
                            } else {
                                attempts++;
                                continue;
                            }
                        }
                    } else if(stream.obj instanceof CreateAccount) {
                        logger.info("Account creation attempt");
                        CreateAccount abc = (CreateAccount)stream.obj;
                        logger.config("Stuck");
                        if(abc.username.equals("nowy") && abc.password.equals("haselko")) {
                            logger.config("Success");
                            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, abc.username, new Ok(Ok.Type.ACCOUNT_CREATED)));
                            output.flush();
                            output.reset();
                            logger.config("Account created");
                        } else {
                            logger.config("Failure");
                            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, abc.username, new Error(Error.Types.LOGIN_EXISTS)));
                            output.flush();
                            output.reset();
                            logger.config("Account creation failure");
                        }
                    } else {
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR,
                                null,
                                new Error(Error.Types.HACK_ATTEMPT)
                        ));
                        output.flush();
                        output.reset();
                        logger.warning("Hack attempt");
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error", e);
                    break;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Error", e);
                }
            }

            ComStream stream;
            Object obj;
            while (!Thread.interrupted() && authenticated) {
                try {
                    obj = null;
                    obj = input.readObject();
                    if (obj != null) {
                        if (!(obj instanceof ComStream)) continue;
                        stream = (ComStream) obj;
                        incomming.add(stream);
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error", e);
                    return;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Error", e);
                    return;
                }
            }
        } finally {
            shutDownNow();
        }
    }

    public synchronized void shutDownNow() {
        running = false;
        try {
            input.close();
            output.close();
            socket.close();
            logger.info("Client " + id + " disconnected");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Very bad!", e);
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
            logger.info("Client" + login.username + " authentication success.");
            return true;
        } else {
            Error e = new Error(Error.Types.NOT_AUTHORIZED);
            output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, login.username, e));
            output.flush();
            output.reset();
            logger.info("Client " + login.username + " authentication failure.");
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
}
