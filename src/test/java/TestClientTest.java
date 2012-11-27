import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;
import de.opitz_consulting.demo.websocket.WebSocketEchoServer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import se.cgbystrom.netty.http.websocket.WebSocketCallback;
import se.cgbystrom.netty.http.websocket.WebSocketClient;
import se.cgbystrom.netty.http.websocket.WebSocketClientFactory;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

import com.google.common.collect.Multiset;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/23/12
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestClientTest {
    @org.junit.Test
    public void testConnect() throws Exception {
        final WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        Set<TestClient> webSocketClients = new CopyOnWriteArraySet<TestClient>();
        String port = "80";

        for (int i = 0; i < 10000; i++) {
            TestClient callback = new TestClient(clientFactory, port);

            webSocketClients.add(callback);
            callback.connect().awaitUninterruptibly();

            assertTrue(callback.waitForConnected(true));
        }


        Multiset<Long> elapsedSet = TreeMultiset.create();


        while (webSocketClients.size() > 0) {
            final TestClient callback = webSocketClients.iterator().next();
            final long t0 = System.currentTimeMillis();
            callback.send(new DefaultWebSocketFrame(TestClient.TEST_MESSAGE));
            assertEquals(TestClient.TEST_MESSAGE, callback.waitFor(1));
            final long t1 = System.currentTimeMillis();
            final long elapsed = t1 - t0;
            elapsedSet.add(elapsed);
            //callback.disconnect();
            //assertFalse(callback.waitForConnected(false));
            webSocketClients.remove(callback);
        }
        System.out.println(elapsedSet);

    }

    private static class TestClient implements WebSocketCallback {
        private final WebSocketClient client;

        private TestClient(WebSocketClientFactory clientFactory, String port) throws URISyntaxException {
            final String host = "ec2-54-247-47-144.eu-west-1.compute.amazonaws.com";
            WebSocketClient client = clientFactory.newClient(new URI("ws://" + host + ":" + port + "/chat"), this);
            this.client = client;
        }

        public String waitFor(int x) {
            while (counter < x) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return messageReceived;
        }

        public boolean waitForConnected(boolean isConnected) {
            while (connected != isConnected) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return connected;
        }

        volatile int counter = 0;
        public static final String TEST_MESSAGE = "Testing this WebSocket";
        volatile boolean connected = false;
        public String messageReceived = null;

        public void onConnect(WebSocketClient client) {
            //System.out.println("WebSocket connected!");
            connected = true;

        }

        public void onDisconnect(WebSocketClient client) {
            System.out.println("WebSocket disconnected!");
            connected = false;
        }

        public void onMessage(WebSocketClient client, WebSocketFrame frame) {
            //System.out.println("Message:" + frame.getTextData());
            counter++;
            messageReceived = frame.getTextData();
        }

        public void onError(Throwable t) {
            t.printStackTrace();
        }


        public ChannelFuture connect() {
            return client.connect().awaitUninterruptibly();
        }

        public void send(DefaultWebSocketFrame defaultWebSocketFrame) {
            this.client.send(defaultWebSocketFrame);
        }

        public void disconnect() {
            this.client.disconnect();
        }
    }
}
