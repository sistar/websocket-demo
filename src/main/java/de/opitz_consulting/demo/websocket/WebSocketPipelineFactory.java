package de.opitz_consulting.demo.websocket;


import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * {@link org.jboss.netty.channel.ChannelPipelineFactory} die alle noetigen {@link org.jboss.netty.channel.ChannelHandler} in die erzeugte
 * {@link org.jboss.netty.channel.ChannelPipeline} einfuegt und diese dan zur Verfuegung stellt.
 * 
 * @author Norman Maurer <norman@apache.org>
 */
public class WebSocketPipelineFactory implements ChannelPipelineFactory {
    private final ChannelGroup group;


    public WebSocketPipelineFactory(ChannelGroup group) {

        this.group = group;

    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {

        ChannelPipeline pipeline = Channels.pipeline();

        SSLEngine engine = WebSocketSslServerSslContext.getInstance().getServerContext().createSSLEngine();
        engine.setUseClientMode(false);
        pipeline.addLast("ssl", new SslHandler(engine));

        // Decoder der ChannelBuffer zu HttpRequest's umwandelt
        pipeline.addLast("reqDecoder", new HttpRequestDecoder());
        
        // Aggregator der HttpChunks' in HttpRequest's aggregiert
        pipeline.addLast("chunkAggregator", new HttpChunkAggregator(65536));
        
        // Encoder der HttpResponse's zu ChannelBuffer umwandelt
        pipeline.addLast("reqEncoder", new HttpResponseEncoder());
        
        // Handler der den richtigen WebSocket Handshaker einfuegt
        // und die Index-Seite zur Verfügung stellt
        pipeline.addLast("handler", new WebSocketServerHandler(group));
        return pipeline;
    }
}
