package me.normanmaurer.javamagazin.netty.examples.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * @author Norman Maurer <norman@apache.org>
 */
public class EchoClient {

    private final byte[] msg = "My Message".getBytes();
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

        Set<ClientBootstrap> bootstraps = new HashSet<ClientBootstrap>();

        for (int i = 0; i < 1 ; i++) {
            final ClientBootstrap bootstrap = startOneClient();
            bootstraps.add(bootstrap);
            logger.info(String.format("clients [%s]", bootstraps.size()));
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        waitForQuit();


        for (ClientBootstrap bootstrap : bootstraps) {
            bootstrap.releaseExternalResources();

        }

    }

    private void close(ChannelFuture future){

        // Freigabe aller Resourcen

            final ChannelFuture closeFuture = future.getChannel().getCloseFuture();
            closeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                }
            });

    }

    private ClientBootstrap startOneClient() {
        final ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new EchoClientHandler());
            }
        });

        // Verbindungsaufbau
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Warten bis Verbindung erfolgreich hergestellt wurde
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ChannelFuture wf = future.getChannel().write(ChannelBuffers.wrappedBuffer(msg));
                    wf.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                                    future.getChannel().write("hello here is the client");
                        }
                    });
                } else {
                    // System.err.println(“Verbindungsaufbau fehlgeschlagen”);
                    future.getCause().printStackTrace();
                    return;

                }
            }
        });
        return bootstrap;
    }

    Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    public void waitForQuit()  {
        String a = "";

        logger.info(String.format("EchoClient  started on host %s port %s",host, port));
        logger.info("Type quit to stop the client");

        Thread reporter = new Thread(new Runnable() {
            public void run() {
                Long freeMemory = Runtime.getRuntime().freeMemory();
                Long maxMemory = Runtime.getRuntime().maxMemory();

                logger.info("free: " + freeMemory / (1024 * 1024) + "max: " + maxMemory / (1024 * 1024) + "percent free: " + (freeMemory * 100) / (maxMemory * 1000 * 1000));
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        reporter.run();


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (!(a.equals("quit"))) {
            try {
                a = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //System.exit(-1);
    }

    class EchoClientHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

            // Logge ein Hex Dump der Nachricht die vom Server via
            // „echo“ zurück geschrieben wurde

            System.out.println("RECV from Server: " + ChannelBuffers.hexDump(
                    (ChannelBuffer) e.getMessage()));
            e.getChannel().close();

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getChannel().close();
        }
    }

    public static void main(String[] args) throws Exception {
        String address;
        int port;
        if (args.length < 2) {
            address = "127.0.0.1";
            port = 8888;
        } else {
            address = args[0];
            port = Integer.parseInt(args[1]);
        }
        new EchoClient(address, port).start();
    }

}