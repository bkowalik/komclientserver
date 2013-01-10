package common.protocol;


public class Message extends ComObject {
    public final String body;

    public Message(String body) {
        this.body = body;
    }
}
