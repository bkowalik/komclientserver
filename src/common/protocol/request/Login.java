package common.protocol.request;

import java.io.Serializable;

import common.protocol.ComObject;


public class Login extends ComObject implements Serializable {
    public final String username;
    public final String password;

    public Login(String login, String password) {
        this.username = login;
        this.password = password;
    }
}
