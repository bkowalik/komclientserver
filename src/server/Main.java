package server;

import server.connection.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        Server s = new Server(44321);
        s.start();
        System.in.read();
        s.stop();
    }
}
