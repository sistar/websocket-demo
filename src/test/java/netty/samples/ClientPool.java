package netty.samples;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import de.opitz_consulting.demo.websocket.SecureChatSslContextFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientPool {

    private final URI uri;

    private final ClientBootstrap bootstrap;

    final Multiset<Long> elapsedSet = TreeMultiset.create();

    final List<WebSocketClientRaw2> testClientsList = new ArrayList<>();

    public ClientPool(URI uri) {
        String protocol = uri.getScheme();
        if (!protocol.equals("ws")) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
        this.uri = uri;
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                // Add SSL handler first to encrypt and decrypt everything.
                // In this example, we use a bogus certificate in the server side
                // and accept any invalid certificates in the client side.
                // You will need something more complicated to identify both
                // and server in the real world.

                SSLEngine engine =
                        SecureChatSslContextFactory.getClientContext().createSSLEngine();
                engine.setUseClientMode(true);

                pipeline.addLast("ssl", new SslHandler(engine));

                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("ws-handler", new WebSocketClientHandler2());
                return pipeline;
            }
        });
    }

    public WebSocketClientRaw2 createClient() throws Exception {
        final WebSocketClientRaw2 raw2 = new WebSocketClientRaw2();
        raw2.connect(uri, bootstrap);

        return raw2;
    }

    public void exit() {
        bootstrap.releaseExternalResources();

    }

    public void generateClients(int i) throws Exception {
        for (int j = 0; j < i; j++) {
            this.testClientsList.add(createClient());
        }
    }

    final Random random = new Random();


    public void sendTestMessagesToRandomClients(int numberOfMessages) throws Exception {
        CountDownLatch responseCountDownLatch = new CountDownLatch(numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            Thread.sleep(10L+random.nextInt(100));
            final WebSocketClientRaw2 raw2 = chooseClientRandomly();
            raw2.sendMessage(String.format("msg %s from client %s", i + 1, raw2),responseCountDownLatch,elapsedSet);
        }
        responseCountDownLatch.await();
    }

    private WebSocketClientRaw2 chooseClientRandomly() {
        final int i = random.nextInt(testClientsList.size());
        return testClientsList.get(i);
    }

    public void close() throws InterruptedException {
        for (WebSocketClientRaw2 raw2 : testClientsList) {
            raw2.close();
        }



    }
}
