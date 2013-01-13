package client;


import client.logic.Connection;
import common.exceptions.UnauthorizedException;
import common.protocol.ComObject;
import common.protocol.ComStream;
import common.protocol.Message;
import common.protocol.request.Login;
import common.protocol.response.Ok;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        runConsole(args);
    }

    public static void runConsole(String[] args) throws IOException, InterruptedException, UnauthorizedException {
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        Scanner in = new Scanner(System.in);

        System.out.print("Login: ");
        String login = in.nextLine();

        System.out.print("Hasło: ");
        String pass = in.nextLine();

        System.out.println("Łączenie...");
        Connection c = new Connection(login);
        c.connect(new InetSocketAddress(host, port), 0);
        System.out.println("Połączono");
        System.out.println("Uwieżytelnianie...");
        ComObject obj = c.authenticate(new Login(login, pass));
        if(!(obj instanceof Ok)) throw new UnauthorizedException();
        System.out.println("Uwieżytelniono");
        System.out.println();

        String to;
        String msg;
        Queue<ComStream> streams = c.getInStreams();
        try {
            while(true) {
                if(!streams.isEmpty()) System.out.println("Odczytuje wiadomości:");
                while(!streams.isEmpty()) {
                    ComStream st = streams.poll();
                    Message m = (Message)st.obj;
                    System.out.println(st.from + ": " + m.body);
                }

                System.out.print("Do: ");
                to = in.nextLine();

                System.out.print("Wiadomość: ");
                msg = in.nextLine();

                if(to.equals("") || msg.equals("")) continue;

                c.sendStream(new ComStream(login, to, new Message(msg)));
            }
        } finally {
            c.disconnect();
        }
    }
}
