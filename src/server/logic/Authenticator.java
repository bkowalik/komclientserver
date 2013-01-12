package server.logic;

import common.protocol.ComStream;

public interface Authenticator {
    boolean freaduDetection(ComStream stream);
    void checkCredentials();
}
