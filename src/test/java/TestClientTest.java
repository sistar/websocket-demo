import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import se.cgbystrom.netty.http.websocket.WebSocketCallback;
import se.cgbystrom.netty.http.websocket.WebSocketClient;
import se.cgbystrom.netty.http.websocket.WebSocketClientFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * User: rsi
 * Date: 11/23/12
 * Time: 5:01 PM
 */
public class TestClientTest {
    @org.junit.Test
    public void testConnect() throws Exception {
        final WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final Set<TestClient> webSocketClients = new CopyOnWriteArraySet<TestClient>();
        final String port = "80";

        Multiset<Long> elapsedSet = TreeMultiset.create();

        //ExecutorService executorService = new ThreadPoolExecutor(8, 8, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8, true), new ThreadPoolExecutor.CallerRunsPolicy());
        final Random random = new Random();

        for (int i = 0; i < 100; i++) {


            final TestClient callback = new TestClient(clientFactory, port, webSocketClients);


            final ChannelFuture connect = callback.connect();
            connect.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    webSocketClients.add(callback);
                   // callback.waitForConnected(true);
                }
            });



        }
        final ArrayList<TestClient> testClientsList = new ArrayList<>(webSocketClients);
        for (int rCounter=0;rCounter<20000;rCounter++){
            final int i = random.nextInt(webSocketClients.size());
            final TestClient testClient =testClientsList.get(i);
            final long t0 = System.currentTimeMillis();
            testClient.send(new DefaultWebSocketFrame(TestClient.TEST_MESSAGE));
            assertEquals(TestClient.TEST_MESSAGE, testClient.waitFor(1));
            final long t1 = System.currentTimeMillis();
            final long elapsed = t1 - t0;
            elapsedSet.add(elapsed);
        }


        final Iterator<TestClient> iterator = webSocketClients.iterator();
        while (iterator.hasNext()) {
            TestClient next = iterator.next();
            next.disconnect();


        }


        //callback.disconnect();
            //assertFalse(callback.waitForConnected(false));
            //webSocketClients.remove(callback);

        System.out.println(elapsedSet);

    }

    private static class TestClient implements WebSocketCallback {
        private final WebSocketClient client;
        private final Set<TestClient> webSocketClients;


        private TestClient(WebSocketClientFactory clientFactory, String port, Set<TestClient> webSocketClients) throws URISyntaxException {
            this.webSocketClients = webSocketClients;
            final String host = "ec2-54-247-47-144.eu-west-1.compute.amazonaws.com";
            //final String host = "localhost";
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

            if (webSocketClients.contains(client)) {
                webSocketClients.remove(client);
            }
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
