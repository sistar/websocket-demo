import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.atomic.AtomicInteger;

/*
 * User: rsi
 * Date: 11/23/12
 * Time: 5:01 PM
 */
public class TestClientTest {
    Logger log = LoggerFactory.getLogger(this.getClass());
    final Multiset<Long> elapsedSet = TreeMultiset.create();
    final AtomicInteger messageCounter = new AtomicInteger();

    @org.junit.Test
    public void testConnect() throws Exception {
        final WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final Set<TestClient> webSocketClients = new CopyOnWriteArraySet<TestClient>();

        final String port = "80";


        //ExecutorService executorService = new ThreadPoolExecutor(8, 8, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8, true), new ThreadPoolExecutor.CallerRunsPolicy());
        final Random random = new Random();

        for (int i = 0; i < 10; i++) {


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
        for (int rCounter = 0; rCounter < 20; rCounter++) {
            final int i = random.nextInt(webSocketClients.size());
            final TestClient testClient = testClientsList.get(i);
            final long t0 = System.currentTimeMillis();
            String msg = String.format("client [%s] timestamp [%s]", i, t0);
            testClient.send(new DefaultWebSocketFrame(msg));
            testClient.waitFor(new Receiver(msg, t0));

        }

        int mCnt = messageCounter.get();
        while (mCnt > 0 ){
            log.info("waiting for {} responses", mCnt);
            Thread.sleep(1000L);
            mCnt = messageCounter.get();
        };


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

    private class TestClient implements WebSocketCallback {
        final Set<Receiver> receivers = new CopyOnWriteArraySet<Receiver>();
        private final WebSocketClient client;
        private final Set<TestClient> webSocketClients;
        private String expected;


        private TestClient(WebSocketClientFactory clientFactory, String port, Set<TestClient> webSocketClients) throws URISyntaxException {
            this.webSocketClients = webSocketClients;
            final String host = "ec2-46-137-59-250.eu-west-1.compute.amazonaws.com";
            //final String host = "localhost";
            WebSocketClient client = clientFactory.newClient(new URI("ws://" + host + ":" + port + "/chat"), this);
            this.client = client;
        }

        public void waitFor(Receiver msg) {
            this.receivers.add(msg);

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
            String message = frame.getTextData();
            for (Receiver receiver : receivers) {
                if (receiver.receive(message)) {
                    receivers.remove(receiver);
                    messageCounter.decrementAndGet();
                }
            }
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
            messageCounter.incrementAndGet();
        }

        public void disconnect() {
            this.client.disconnect();
        }
    }

    private class Receiver {

        private final String expected;
        private final long t0;

        private Receiver(String expected, long t0) {
            this.expected = expected;
            this.t0 = t0;
        }

        public boolean receive(String message) {
            final long t1 = System.currentTimeMillis();
            final long elapsed = t1 - t0;

            final boolean msgMatched = this.expected.equals(message);
            if (msgMatched) {
                elapsedSet.add(elapsed);
                return true;
            }
            return false;
        }
    }
}
