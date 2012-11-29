package netty.samples;

import com.google.common.collect.Multiset;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.websocketx.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class WebSocketClientRaw2 {

    Channel ch = null;

    public void connect(URI uri, ClientBootstrap bootstrap) throws Exception {
        HashMap<String, String> customHeaders = new HashMap<String, String>();

        final WebSocketClientHandshaker handshaker =
                new WebSocketClientHandshakerFactory().newHandshaker(
                        uri, WebSocketVersion.V13, null, false, customHeaders);


        // Connect
        System.out.printf("WebSocket Client connecting  %s \n",this);
        ChannelFuture future =
                bootstrap.connect(
                        new InetSocketAddress(uri.getHost(), uri.getPort()));
        future.syncUninterruptibly();

        ch = future.getChannel();
        final ChannelHandler wsChannelHandler = getChannelHandler();
        WebSocketClientHandler2 handler2 = (WebSocketClientHandler2) wsChannelHandler;
        handler2.setHandshaker(handshaker);

        handshaker.handshake(ch).syncUninterruptibly();

    }

    public WebSocketClientHandler2 getChannelHandler() {
        final WebSocketClientHandler2 handler2 = (WebSocketClientHandler2) ch.getPipeline().get("ws-handler");
        return handler2;

    }

    public void sendMessage(String message, CountDownLatch responseCountDownLatch, Multiset<Long> elapsedSet) throws Exception {
        System.out.printf("WebSocket Client %s sending  %s \n",this,message);
        injectTimer(message,responseCountDownLatch,elapsedSet);
        ch.write(new TextWebSocketFrame(message));
    }

    private void injectTimer(final String message, final CountDownLatch responseCountDownLatch, Multiset<Long> elapsedSet) {
        final long t0 = System.currentTimeMillis();
        this.getChannelHandler().addReceivedCallback(new TimerReceiver(message, t0,responseCountDownLatch,elapsedSet,this) );

    }

    public void ping() {

        // Ping
        System.out.println("WebSocket Client sending ping");
        ch.write(new PingWebSocketFrame(ChannelBuffers.copiedBuffer(new byte[]{1, 2, 3, 4, 5, 6})));


    }

    public void close() {
        try {
            System.out.println("WebSocket Client sending close");
            ch.write(new CloseWebSocketFrame());

            // WebSocketClientHandler will close the connection when the server
            // responds to the CloseWebSocketFrame.
            final ChannelFuture channelFuture = ch.getCloseFuture();
             channelFuture.awaitUninterruptibly();
        } finally {
            if (ch != null) {
                ch.close();
            }
        }
    }


}