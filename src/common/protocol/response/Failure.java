package common.protocol.response;


import common.protocol.ComObject;

public class Failure extends ComObject {
    public static enum Types {
        NOT_AUTHORIZED, INTERNAL_SERVER_ERROR, CONNECTION_RESET, HACK_ATTEMPT, LOGIN_EXISTS;
    }

    public final Types type;

    public Failure(Types type) {
        this.type = type;
    }
}
