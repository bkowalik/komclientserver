package common.protocol;


import java.util.Date;

public class Message extends ComObject {
    public final String body;
    public final Date date;

    public Message(String body) {
        this.body = body;
        date = new Date();
    }

    public Message(String body, Date date) {
        this.body = body;
        this.date = date;
    }
}
