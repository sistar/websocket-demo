package netty.samples;

import com.google.common.collect.Multiset;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

public class WebSocketClientHandler2 extends SimpleChannelUpstreamHandler {

    private WebSocketClientHandshaker handshaker = null;
    private Set<Receiver> receivedCallbacks = new CopyOnWriteArraySet<Receiver>();


    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Channel ch = ctx.getChannel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
            System.out.println("WebSocket Client connected!");
            return;
        }

        if (e.getMessage() instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();
            throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
                    + response.getContent().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            for (Receiver receivedCallback : receivedCallbacks) {
                receivedCallback.receive(textFrame.getText());
            }


        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        t.printStackTrace();
        e.getChannel().close();
    }

    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }


    public void addReceivedCallback(Receiver receivedCallback) {
        this.receivedCallbacks.add(receivedCallback);
    }

    private Receiver findCallback(String message) {
        for (Receiver receivedCallback : receivedCallbacks) {
            final boolean match = receivedCallback.expectedMessage.equals(message);
            if (match) {
                return receivedCallback;
            }
        }
        return null;
    }

    public void removeFinishedCallback(TimerReceiver timerReceiver, String message, long elapsed, CountDownLatch responseCountDownLatch, Multiset<Long> elapsedSet) {
        final Receiver callback = findCallback(message);
        if (receivedCallbacks.remove(callback)) {

            System.out.printf("client: %s msg: [%s] received matched\n", this, message);
            elapsedSet.add(elapsed);
            responseCountDownLatch.countDown();
            System.out.printf("latch now at %s", responseCountDownLatch.getCount());
        } else {
            System.out.printf("no match for %s in %s", message, receivedCallbacks);

        }

    }
}