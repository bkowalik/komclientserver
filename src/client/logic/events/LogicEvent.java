package client.logic.events;


import java.util.EventObject;

public class LogicEvent extends EventObject {
    public final Type type;

    public static enum Type {
        AUTH_SUCCESS, AUTH_FAILURE, DISCONNECT, CONNECT, ACCOUNT_CREATED, ACCOUNT_EXIST;
    }

    public LogicEvent(Object source, Type t) {
        super(source);
        type = t;
    }
}
