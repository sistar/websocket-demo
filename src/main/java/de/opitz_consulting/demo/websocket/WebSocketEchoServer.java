package de.opitz_consulting.demo.websocket;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * User: rsi
 * Date: 11/6/12
 * Time: 12:01 PM
 */
public class WebSocketEchoServer {
    private static final String WEBSOCKET_PATH = "/ws";
    private final ChannelGroup group = new DefaultChannelGroup();


    private final int port;
    Logger log = LoggerFactory.getLogger(this.getClass());

    public WebSocketEchoServer(int port
    ) {
        this.port = port;
    }

    public void startUp() {

        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory());

        bootstrap.setPipelineFactory(new WebSocketPipelineFactory(group));
        // Bind die Lokale Adresse
        final Channel bind = bootstrap.bind(new InetSocketAddress(port));
        log.info("starting websocket server on port {} ",port);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int prevGroupSize=0;
                while (true){
                    try {
                        Thread.sleep(5000L);
                        if(group.size() != prevGroupSize){
                            log.info("group size {}", group.size());
                            prevGroupSize= group.size();
                        }


                        final Random random = new Random();
                        final int i = random.nextInt(group.size()+1);

                        final Channel channel = group.find(i);
                        if(channel != null){
                            log.info("msg for client {}",channel.getId());
                            channel.write(String.format("msg for client %s",channel.getId())    );
                        }

                    } catch (InterruptedException e) {
                        log.error("interrupted ",e);
                    }
                }
            }
        })      .run();

    }

    public static void main(String[] args) {
        int wsPort;
        int udpPort;
        if (args.length < 2) {
            wsPort = 8080;

        } else {
            wsPort = Integer.parseInt(args[0]);
            udpPort = Integer.parseInt(args[1]);
        }
        new WebSocketEchoServer(wsPort).startUp();
    }


}
