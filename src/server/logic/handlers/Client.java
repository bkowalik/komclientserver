package server.logic.handlers;

import common.protocol.ComStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client {
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    protected final Socket socket;

    public Client(Socket soc) throws IOException {
        socket = soc;
        output = new ObjectOutputStream(new BufferedOutputStream(soc.getOutputStream()));
        input = new ObjectInputStream(soc.getInputStream());
    }

    public void sendMessage(ComStream stream) throws IOException {
        output.writeObject(stream);
        output.flush();
        output.reset();
    }

    public int avaliable() throws IOException {
        return input.available();
    }
}
