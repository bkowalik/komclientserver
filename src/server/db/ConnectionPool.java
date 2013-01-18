package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {
    private static final int MAX_THREADS = 5;
    private final ExecutorService exec = Executors.newFixedThreadPool(MAX_THREADS);
    private final BlockingQueue<PreparedStatement> toPorcess = new LinkedBlockingQueue<PreparedStatement>();
    private final BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
    private boolean running;

    public ConnectionPool() {
    }

    public void addStatement(PreparedStatement ps) {
        toPorcess.add(ps);
    }

    public synchronized void start() {
        running = true;
        for(int i = 0; i < MAX_THREADS; i++) {

        }
    }

    public synchronized void stop() {
        running = false;
        exec.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection con = DriverManager.getConnection("jdbc:sqlite:/home/bartek/myDb.db");

        con.close();
    }
}
