package server.logic;

import common.protocol.ComStream;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientWorker implements Runnable {
    public final String id;
    private final Queue<ComStream> toSend = new ConcurrentLinkedQueue<ComStream>();
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    public ClientWorker(String id, Socket socket) throws IOException {
        this(id, socket, new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())),
                new ObjectInputStream(socket.getInputStream()));
    }

    public ClientWorker(String id, Socket socket, OutputStream output, InputStream input) throws IOException {
        this(id, socket, new ObjectOutputStream(new BufferedOutputStream(output)),
                new ObjectInputStream(input));
    }

    public ClientWorker(String id, Socket socket, ObjectOutputStream output, ObjectInputStream input) throws IOException {
        this.id = id;
        this.output = output;
        this.input = input;
        this.socket = socket;
        this.socket.setSoTimeout(Server.DEFAULT_IDLE_TIMEOUT);
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
        }
    }

    public void sendResponse(ComStream comObj) {
        toSend.add(comObj);
    }

    @Override
    public String toString() {
        return id;
    }
}
