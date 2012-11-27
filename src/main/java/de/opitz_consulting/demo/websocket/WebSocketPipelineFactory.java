package de.opitz_consulting.demo.websocket;


import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.util.Map;

/**
 * {@link org.jboss.netty.channel.ChannelPipelineFactory} die alle noetigen {@link org.jboss.netty.channel.ChannelHandler} in die erzeugte
 * {@link org.jboss.netty.channel.ChannelPipeline} einfuegt und diese dan zur Verfuegung stellt.
 * 
 * @author Norman Maurer <norman@apache.org>
 */
public class WebSocketPipelineFactory implements ChannelPipelineFactory {
    private final ChannelGroup group;
    private final Map<String, Integer> clientToChannel;

    public WebSocketPipelineFactory(ChannelGroup group,Map<String,Integer> clientToChannel) {

        this.group = group;
        this.clientToChannel = clientToChannel;
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Pipeline Object erstellen
        ChannelPipeline pipeline = Channels.pipeline();
        
        // Decoder der ChannelBuffer zu HttpRequest's umwandelt
        pipeline.addLast("reqDecoder", new HttpRequestDecoder());
        
        // Aggregator der HttpChunks' in HttpRequest's aggregiert
        pipeline.addLast("chunkAggregator", new HttpChunkAggregator(65536));
        
        // Encoder der HttpResponse's zu ChannelBuffer umwandelt
        pipeline.addLast("reqEncoder", new HttpResponseEncoder());
        
        // Handler der den richtigen WebSocket Handshaker einfuegt
        // und die Index-Seite zur Verfügung stellt
        pipeline.addLast("handler", new WebSocketServerHandler(group,clientToChannel));
        return pipeline;
    }
}
