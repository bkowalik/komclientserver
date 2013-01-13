import client.logic.Connection;
import common.protocol.ComObject;
import common.protocol.request.Login;
import common.protocol.response.Error;
import common.protocol.response.Ok;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import server.logic.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

@RunWith(JUnit4.class)
public class ClientServerTest {
    private Connection client;
    private Server server;

    @Before
    public void beforeTest() {
        try {
            server = new Server(44321);
            server.start();

            client = new Connection("Client 1");
            client.connect(new InetSocketAddress("localhost", 44321), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void authentication() {
        try {
            ComObject obj = client.authenticate(new Login("bartek", "zle"));
            org.junit.Assert.assertNotNull(obj);
            if(obj instanceof Ok) {
                System.out.println("Ok response");
                Ok ok = (Ok) obj;
                org.junit.Assert.assertEquals(Ok.Type.AUTHENTICATED, ok.type);
            }
            if(obj instanceof common.protocol.response.Error) {
                System.out.println("Error respones");
                common.protocol.response.Error e = (common.protocol.response.Error) obj;
                org.junit.Assert.assertEquals(Error.Types.NOT_AUTHORIZED, e.type);
            }
        } catch(InterruptedException e) { e.printStackTrace(); }
    }


    @After
    public void afterTest() {
        client.disconnect();
        server.stop();
    }
}
