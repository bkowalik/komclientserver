package common.protocol;

import java.io.Serializable;

public class ComStream implements Serializable {
    public final String from;
    public final String to;
    public final ComObject obj;

    public ComStream(String from, String to, ComObject obj) {
        this.from = from;
        this.to = to;
        this.obj = obj;
    }
}
