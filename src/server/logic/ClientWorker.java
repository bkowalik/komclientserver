package server.logic;

import common.protocol.ComStream;
import common.protocol.request.Login;
import common.protocol.response.Error;
import common.protocol.response.Ok;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientWorker.class);
    private  String id;
    private Authenticator authenticator;
    private boolean running;
    private boolean authenticated;
    private final Queue<ComStream> toSend = new ConcurrentLinkedQueue<ComStream>();
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    public ClientWorker(Socket socket, Authenticator authenticator) throws IOException {
        this(null, socket, new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())),
                new ObjectInputStream(socket.getInputStream()));
        this.authenticator = authenticator;
    }

    public ClientWorker(String id, Socket socket, OutputStream output, InputStream input) throws IOException {
        this(id, socket, new ObjectOutputStream(new BufferedOutputStream(output)),
                new ObjectInputStream(input));
    }

    public ClientWorker(String id, Socket socket, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        this.id = id;
        this.output = output;
        this.output.flush();
        this.input = input;
        this.socket = socket;
        this.socket.setSoTimeout(Server.DEFAULT_IDLE_TIMEOUT);
    }

    @Override
    public void run() {
        running = true;
        try {
            try { socket.setSoTimeout(Server.DEFAULT_IDLE_TIMEOUT); } catch (IOException e) { logger.warn("Error", e); }
            logger.debug("Starting authentication");
            while(!Thread.interrupted() && !authenticated) {
                try {
                    ComStream stream = (ComStream) input.readObject();
                    if(!(stream.obj instanceof Login)) {
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR,
                                null,
                                new Error(Error.Types.HACK_ATTEMPT)
                        ));
                        output.flush();
                        output.reset();
                        return;
                    }

                    Login login = (Login) stream.obj;
                    if(login.login.equals("bartek") && login.password.equals("haslo")) {
                        authenticated = true;
                        Ok ok = new Ok(Ok.Type.AUTHENTICATED);
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, null, ok));
                        output.flush();
                        output.reset();
                        break;
                    } else {
                        Error e = new Error(Error.Types.NOT_AUTHORIZED);
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, null, e));
                        output.flush();
                        output.reset();
                    }
                } catch (IOException e) {
                    logger.warn("Error", e);
                } catch (ClassNotFoundException e) {
                    logger.warn("Error", e);
                }
            }

            try { socket.setSoTimeout(0); } catch (IOException e) { e.printStackTrace(); }
            while(!Thread.interrupted() && authenticated) {

            }
        } finally {
            try {
                running = false;
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) { }
        }
    }

    public boolean isActive() {
        return running;
    }

    public void sendResponse(ComStream comObj) {
        toSend.add(comObj);
    }

    @Override
    public String toString() {
        return id;
    }
}
