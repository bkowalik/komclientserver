package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
                connection = DriverManager.getConnection("jdbc:sqlite" + database);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failure", e);
                return;
            }

            try {
                while(!Thread.interrupted()) {
                    while(!working){
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE, "Failure", e);
                            break;
                        }
                    }


                    // TODO: Przetworzenie requesta do bazy danych



                    working = false;
                }
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                }
            }
        }

        public synchronized void work() {
            working = true;
            notifyAll();
        }

        public boolean isWorking() {
            return working;
        }
    }

    private static final Logger logger = Logger.getLogger(DBTask.class.getName());
    private static final int MAX_THREADS = 5;
    private ExecutorService exec = Executors.newFixedThreadPool(MAX_THREADS);
    private final BlockingQueue<DBTask> workers = new ArrayBlockingQueue<DBTask>(MAX_THREADS);
    private boolean running;
    private final String database;

    public DBManager(String database) throws ClassNotFoundException {
        this.database = database;
        Class.forName("org.sqlite.JDBC");
    }

    public void start() {
        for(int i = 0; i < MAX_THREADS; i++) {
            DBTask task = new DBTask(database);
            exec.execute(task);
            workers.add(task);
        }
        running = true;
    }

    public void stop() {
        exec.shutdownNow();
        running = false;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection con = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Bartek\\Desktop\\mydb2");
        con.close();
    }
}
