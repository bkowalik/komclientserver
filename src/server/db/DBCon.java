package server.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DBCon implements Runnable {
    private static final Logger logger = Logger.getLogger(DBCon.class.getName());
    private Connection con;


    public DBCon() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:simple:simple.db");
    }

    public synchronized void disconnect() throws SQLException {
        con.close();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {

        }
    }
}
