package server.logic;


import common.protocol.ComStream;

public class ClinetAuthenticator implements Authenticator {

    @Override
    public boolean freaduDetection(ComStream stream) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void checkCredentials() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
