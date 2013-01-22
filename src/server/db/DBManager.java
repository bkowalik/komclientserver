package server.db;

import common.protocol.ComObject;
import common.protocol.ComStream;
import common.protocol.Message;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import server.ServerLogger;

import javax.sql.RowSet;
import java.sql.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {

    private class DBTask implements Runnable {
        private Connection connection;
        private boolean working;
        private String database;

        public DBTask(String database) {
            this.database = database;
        }

        @Override
        public synchronized void run() {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + database);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failure", e);
                throw new NullPointerException("Database file exception");
            }

            try {
                ComObject obj;
                while(!Thread.interrupted()) {
                    obj = null;
                    try {
                        obj = requests.take();
                    } catch (InterruptedException e) {
                        logger.log(Level.FINEST, "ERROR", e);
                    }

                    if(obj instanceof Login) {
                        Login login = (Login) obj;
                    } else if(obj instanceof CreateAccount) {
                        CreateAccount account = (CreateAccount) obj;
                    }
                }
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                }
            }
        }

        private boolean dbLogin(Login login) throws SQLException {
            String statement = "SELECT `password` FROM `users` WHERE `username` = ?";
            PreparedStatement ps = connection.prepareStatement(statement);
            ps.setString(1, login.username);
            ResultSet rowSet = ps.executeQuery();

            if(rowSet.next()) return false;

            if(login.password.equals(rowSet.getString("password"))) {
                return true;
            } else {
                return false;
            }
        }

        private PreparedStatement dbCreateAcc(CreateAccount account) throws SQLException {
            String statement = "";
            return connection.prepareStatement(statement);
        }

        private PreparedStatement dbStoreMsg(Message msg) throws SQLException {
            String statement = "";
            return connection.prepareStatement(statement);
        }

        public synchronized void work() {
            working = true;
            notifyAll();
        }

        public boolean isWorking() {
            return working;
        }
    }

    private static final Logger logger = ServerLogger.getLogger();
    private int maxThreads = 5;
    private ExecutorService exec;
    private final BlockingQueue<DBTask> workers;
    private final Queue<ComStream> icomming;
    private final BlockingQueue<ComObject> requests;
    private boolean running;
    private final String database;

    public DBManager(String database, Queue<ComStream> incomming, int maxThreads) {
        this.database = database;
        this.icomming = incomming;
        this.maxThreads = maxThreads;
        requests = new LinkedBlockingQueue<ComObject>();
        workers = new ArrayBlockingQueue<DBTask>(maxThreads);
        exec = Executors.newFixedThreadPool(maxThreads);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINEST, "Error", e);
            throw new NullPointerException("Database null!");
        }
    }

    public void start() {
        for(int i = 0; i < maxThreads; i++) {
            DBTask task = new DBTask(database);
            exec.execute(task);
            workers.add(task);
        }
        running = true;
        logger.info("DBThreads started");
    }

    public void stop() {
        exec.shutdownNow();
        running = false;
        logger.info("DBThreads stopped");
    }

    public void postRequest(ComObject obj) {
        requests.add(obj);
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection con = DriverManager.getConnection("jdbc:sqlite:myDatabase");

        PreparedStatement ps = con.prepareStatement("SELECT `password` FROM `users` WHERE `username`=?");
        ps.setString(1, "bartek");
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            System.out.println(rs.getString("password"));
        }

        con.close();
    }
}
