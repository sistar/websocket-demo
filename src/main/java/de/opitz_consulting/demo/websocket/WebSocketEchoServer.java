package de.opitz_consulting.demo.websocket;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * User: rsi
 * Date: 11/6/12
 * Time: 12:01 PM
 */
public class WebSocketEchoServer {

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
                            channel.write(String.format("ms" +
                                    "g for client %s",channel.getId())    );
                        }

                    } catch (InterruptedException e) {
                        log.error("interrupted ",e);
                    }
                }
            }
        })      .run();

    }

    public static void main(String[] args) {
        int wsPort=8443;
        if (args.length > 0) {
            wsPort = Integer.parseInt(args[0]);
        }

        String keyStoreFilePath = System.getProperty("keystorefilepath");
        if (keyStoreFilePath == null || keyStoreFilePath.isEmpty()) {
            System.out.println("ERROR: System property keystorefilepath not set. Exiting now!");
            System.exit(1);
        }

        String keyStoreFilePassword = System.getProperty("keystorefilepassword");
        if (keyStoreFilePassword == null || keyStoreFilePassword.isEmpty()) {
            System.out.println("ERROR: System property keystore.file.password not set. Exiting now!");
            System.exit(1);
        }

        new WebSocketEchoServer(wsPort).startUp();
    }
}
