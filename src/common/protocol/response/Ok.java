package common.protocol.response;

import common.protocol.ComObject;


public class Ok extends ComObject {
    public static enum Type {
        AUTHENTICATED, ACCOUNT_CREATED;
    }

    public final Type type;

    public Ok(Type type) {
        this.type = type;
    }
}
