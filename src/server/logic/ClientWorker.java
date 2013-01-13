package server.logic;

import common.protocol.ComStream;
import common.protocol.request.Login;
import common.protocol.response.Error;
import common.protocol.response.Ok;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientWorker.class.getName());
    private  String id;
    private boolean running;
    private boolean authenticated;
    private final BlockingQueue<ComStream> incomming;
    private final BlockingQueue<ComStream> toSend = new LinkedBlockingQueue<ComStream>();
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    public ClientWorker(Socket socket, BlockingQueue<ComStream> incomming) throws IOException {
        if(socket == null) throw new NullPointerException("Socket cannot be null");
        if(incomming == null) throw new NullPointerException("Incomming messages queue cannot be null");

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
                    if((login.username.equals("bartek") && login.password.equals("haslo"))
                            || (login.username.equals("misia") && login.password.equals("maslo"))) {
                        authenticated = true;
                        id = login.username;
                        Ok ok = new Ok(Ok.Type.AUTHENTICATED);
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, login.username, ok));
                        output.flush();
                        output.reset();
                        break;
                    } else {
                        Error e = new Error(Error.Types.NOT_AUTHORIZED);
                        output.writeObject(new ComStream(Server.SERVER_IDENTYFICATOR, login.username, e));
                        output.flush();
                        output.reset();
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error", e);
                    break;
                } catch (ClassNotFoundException e) {
                    logger.log(Level.WARNING, "Error", e);
                    break;
                }
            }

            ComStream stream;
            Object obj;
            try { socket.setSoTimeout(5); } catch (IOException e) { e.printStackTrace(); }
            while(!Thread.interrupted() && authenticated) {
                try {
                    if(toSend.size() > 0) {
                        System.out.println("Wiadomości");
                        for(ComStream s : toSend) {
                            output.writeObject(toSend.take());
                            output.flush();
                            output.reset();
                        }
                        toSend.clear();
                    }
                    obj = null;
                    obj = input.readObject();
                    if(obj != null) {
                        if(!(obj instanceof ComStream)) continue;
                        stream = (ComStream) obj;
                        incomming.add(stream);
                    }
                } catch (SocketTimeoutException e) {
                } catch(IOException e) {
                    logger.log(Level.WARNING, "Error", e);
                    return;
                } catch(InterruptedException e) {
                    logger.log(Level.WARNING, "Error", e);
                    return;
                } catch(ClassNotFoundException e) {
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
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Very bad!", e);
        }
    }

    public synchronized boolean isActive() {
        return running;
    }

    public synchronized boolean isAuthenticated() {
        return authenticated;
    }

    public void sendResponse(ComStream comObj) {
        toSend.add(comObj);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
