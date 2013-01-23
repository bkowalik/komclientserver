package common.protocol.request;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import common.protocol.ComObject;


public class Login extends ComObject implements Serializable {
    public final String username;
    public final String password;

    public Login(String login, String password) {
        this.username = login;
        this.password = hashPassword(password);
    }

    protected static String hashPassword(String pass) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {}
        md5.update(pass.getBytes());
        StringBuffer buffer = new StringBuffer();
        byte[] digest = md5.digest();
        for(int i = 0; i < digest.length; i++) {
            buffer.append(Integer.toHexString(0xff & digest[i]));
        }
        return buffer.toString();
    }
}
