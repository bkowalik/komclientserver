package common.protocol.request;

import common.protocol.ComObject;

import java.io.Serializable;


public class Login extends ComObject implements Serializable {
    public final String username;
    public final String password;

    public Login(String login, String password) {
        this.username = login;
        this.password = password;
    }
}
