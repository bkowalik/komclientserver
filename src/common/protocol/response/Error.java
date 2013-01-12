package common.protocol.response;


import common.protocol.ComObject;

public class Error extends ComObject {
    public static enum Types {
        NOT_AUTHORIZED, INTERNAL_SERVER_ERROR, CONNECTION_RESET, HACK_ATTEMPT;
    }

    public final Types type;

    public Error(Types type) {
        this.type = type;
    }
}
