package client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Logger;

import client.gui.MainWindow;
import client.logic.Connection;

import common.exceptions.UnauthorizedException;
import common.protocol.ComObject;
import common.protocol.ComStream;
import common.protocol.Message;
import common.protocol.request.Login;
import common.protocol.response.Ok;

import javax.swing.*;

import static client.ClientLogger.*;

public class Main {
    private static Logger logger = ClientLogger.getLogger();

    public static void main(String[] args) {
//        runConsole(args);
        init();
        runGUI();
    }
    
    public static void runGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow mw = new MainWindow();
                mw.setVisible(true);
            }
        });
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
        Connection c = new Connection();
        c.connect(new InetSocketAddress(host, port), 0);
        System.out.println("Połączono");
        System.out.println("Uwieżytelnianie...");
        ComObject obj = c.authenticate(new Login(login, pass));
        if (!(obj instanceof Ok)) throw new UnauthorizedException();
        System.out.println("Uwierzytelniono");
        System.out.println();

        String to;
        String msg;
        Queue<ComStream> streams = c.getInStreams();
        try {
            while (true) {
                if (!streams.isEmpty()) System.out.println("Odczytuję wiadomości:");
                while (!streams.isEmpty()) {
                    ComStream st = streams.poll();
                    Message m = (Message) st.obj;
                    System.out.println(st.from + ": " + m.body);
                }

                System.out.print("Do: ");
                to = in.nextLine();

                System.out.print("Treść: ");
                msg = in.nextLine();

                if (to.equals("") || msg.equals("")) continue;

                c.sendStream(new ComStream(login, to, new Message(msg)));
            }
        } finally {
            c.disconnect();
        }
    }
}
