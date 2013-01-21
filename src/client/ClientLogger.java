package client;


import java.util.logging.*;

public final class ClientLogger {
    private static final Logger logger = Logger.getLogger(ClientLogger.class.getName());
    private static Handler handler = new ConsoleHandler();
    private static Level level = Level.ALL;

    public static void init() {
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(level);
        logger.addHandler(handler);
    }

    public static Logger getLogger() {
        return logger;
    }
}
