package client.logic.events;

import common.protocol.ComStream;

import java.util.EventObject;

public class MessageEvent extends EventObject {
    public final ComStream stream;

    public MessageEvent(Object source, ComStream stream) {
        super(source);
        this.stream = stream;
    }
}
