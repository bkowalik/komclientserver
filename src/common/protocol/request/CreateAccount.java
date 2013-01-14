package common.protocol.request;


import common.protocol.ComObject;

public class CreateAccount extends ComObject {
    public final String username;
    public final String password;

    public CreateAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
