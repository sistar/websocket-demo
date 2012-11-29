
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.junit.Test;
import se.cgbystrom.netty.http.websocket.WebSocketCallback;
import se.cgbystrom.netty.http.websocket.WebSocketClient;
import se.cgbystrom.netty.http.websocket.WebSocketClientFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/28/12
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCallbackTest {
    @Test
    public void testThatOnlyTheGivenCallBackIsCalledBack() throws Exception {


        final WebSocketClientFactory clientFactory = new WebSocketClientFactory();


        final String port = "80";

        final AtomicInteger responseCounterA = new AtomicInteger();
        final AtomicInteger responseCounterB = new AtomicInteger();

        final String host = "ec2-46-137-59-250.eu-west-1.compute.amazonaws.com";
        final URI uri = new URI("ws://" + host + ":" + port + "/chat");
        WebSocketClient a = clientFactory.newClient(uri, new WebSocketCallback() {
            @Override
            public void onConnect(WebSocketClient client) {

            }

            @Override
            public void onDisconnect(WebSocketClient client) {

            }


            @Override
            public void onMessage(WebSocketClient client, WebSocketFrame frame) {
                       responseCounterA.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {

            }
        });

        WebSocketClient b = clientFactory.newClient(uri, new WebSocketCallback() {
            @Override
            public void onConnect(WebSocketClient client) {

            }

            @Override
            public void onDisconnect(WebSocketClient client) {

            }

            @Override
            public void onMessage(WebSocketClient client, WebSocketFrame frame) {
                responseCounterB.incrementAndGet();
            }

            @Override
            public void onError(Throwable t) {

            }
        });

        a.connect().awaitUninterruptibly();
        b.connect().awaitUninterruptibly();
        a.send(new DefaultWebSocketFrame("to A"));
        Thread.sleep(4000L);
        assertThat(responseCounterA.get(), is(equalTo(1)));


    }
}
