package server.db;


import common.protocol.ComStream;
import common.protocol.Message;
import common.protocol.request.CreateAccount;
import common.protocol.request.Login;
import server.ServerLogger;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {
    private static final Logger logger = ServerLogger.getLogger();
    private static final int MAX_CONNECTIONS = 5;
    private final BlockingQueue<Connection> avaliable = new ArrayBlockingQueue<Connection>(MAX_CONNECTIONS);
    private final String database;

    public DBManager(String database) {
        this.database = database;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINEST, "Error", e);
            throw new NullPointerException("JDBC loading failed");
        }
    }

    public final void initialize() throws SQLException {
        for(int i = 0; i < MAX_CONNECTIONS; i++) {
            avaliable.add(DriverManager.getConnection("jdbc:sqlite:" + database));
        }

    }

    public final void free() {
        try {
            for(int i = 0; i < MAX_CONNECTIONS; i++) {
                avaliable.take().close();
            }
        } catch(Exception e) {
            logger.log(Level.FINER, "Error", e);
        }
    }

    public final boolean dbLogin(Login login) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rowSet = null;
        try {
            con = getFreeConnection();
        } catch (InterruptedException e) {
            logger.log(Level.FINEST, "Error", e);
            return false;
        }
        try {
            String statement = "SELECT password FROM users WHERE username = ?";
            ps = con.prepareStatement(statement);
            ps.setString(1, login.username);
            rowSet = ps.executeQuery();

            if(login.password.equals(rowSet.getString("password"))) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.FINEST, "Error", e);
        } finally {
            avaliable.add(con);
            if(rowSet != null) try {
                rowSet.close();
                if(ps != null) ps.close();

            } catch (SQLException e) {
                logger.log(Level.FINEST, "Error", e);
            }
        }

        return false;
    }

    private boolean dbCreateAcc(CreateAccount account) throws SQLException {
        String statement = "";
        return false;
    }

    public final void dbStoreMsg(String from, String to, String msg, long date) throws InterruptedException {
        Connection con = getFreeConnection();
        PreparedStatement ps = null;
        try {
            String statement = "INSERT INTO messages (from_user, to_user, message_body, send) VALUES(?,?,?,?)";
            ps = con.prepareStatement(statement);
            ps.setString(1, from);
            ps.setString(2, to);
            ps.setString(3, msg);
            ps.setLong(4, date);
            ps.executeUpdate();
        } catch(SQLException e) {
            logger.log(Level.FINEST, "Error", e);
        } finally {
            avaliable.add(con);
            if(ps != null) try {
                ps.close();
            } catch (SQLException e) {
                logger.log(Level.FINEST, "Error", e);
            }
        }
    }

    public final List<ComStream> getPendingMessages(String id) {
        Connection con = null;
        List<ComStream> pending = new LinkedList<ComStream>();

        try {
            con = getFreeConnection();
        } catch (InterruptedException e) {
            logger.log(Level.FINEST, "Error", e);
            return null;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String statement = "SELECT * FROM messages WHERE to_user = ?";
            ps = con.prepareStatement(statement);
            ps.setString(1, id);
            rs = ps.executeQuery();

            while(rs.next()) {
                String from = rs.getString("from_user");
                String to = rs.getString("to_user");
                String body = rs.getString("message_body");
                Date date = new Date(rs.getLong("send"));
                ComStream cs = new ComStream(from, to, new Message(body, date));
                pending.add(cs);
            }

            rs.close();
            ps.close();

            if(pending.isEmpty()) return pending;

            statement = "DELETE FROM messages WHERE to_user = ?";
            ps = con.prepareStatement(statement);
            ps.setString(1, id);
            int i = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            logger.log(Level.FINEST, "Error", e);
            return pending;
        } finally {
            avaliable.add(con);
            try {
                if(rs != null) rs.close();
                if(ps != null) ps.close();
            } catch (SQLException e) {
                logger.log(Level.FINEST, "Error", e);
            }
        }

        return pending;
    }

    protected Connection getFreeConnection() throws InterruptedException {
        return avaliable.take();
    }

    protected void releaseConnection(Connection con) {
        avaliable.add(con);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String haslo = "admin1";
        md5.update(haslo.getBytes());
        StringBuffer buffer = new StringBuffer();
        byte[] digest = md5.digest();
        for(int i = 0; i < digest.length; i++) {
            buffer.append(Integer.toHexString(0xff & digest[i]));
        }
        System.out.println(buffer.toString());
    }
}
