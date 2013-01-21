package server;


import java.util.logging.*;

public final class ServerLogger {
    private static final Logger logger = Logger.getLogger("Server");
    private static final Handler handler = new ConsoleHandler();
    private static final Formatter formatter = new SimpleFormatter();
    private static final Level level = Level.ALL;

    public static void initLogger() {
        handler.setFormatter(formatter);
        handler.setLevel(level);
        logger.addHandler(handler);
    }

    public static Logger getLogger() {
        logger.setLevel(level);
        return logger;
    }
}
