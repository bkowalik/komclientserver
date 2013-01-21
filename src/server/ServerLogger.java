package server;


import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class ServerLogger {
    public static final Logger logger = Logger.getLogger(ServerLogger.class.getName());
    static {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINEST);
        ch.setFormatter(new SimpleFormatter());
        logger.addHandler(ch);
    }
}
