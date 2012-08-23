package me.normanmaurer.javamagazin.netty.examples.echo;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * 
 * @author Norman Maurer <norman@apache.org>
 *
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
        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new EchoClientHandler());
            }
        });

        // Verbindungsaufbau
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Warten bis Verbindung erfolgreich hergestellt wurde
        future.awaitUninterruptibly();

        if (future.isSuccess()) {
            ChannelFuture wf = future.getChannel().write(ChannelBuffers.wrappedBuffer(msg));
            wf.awaitUninterruptibly();
        } else {
            // System.err.println(“Verbindungsaufbau fehlgeschlagen”);
            future.getCause().printStackTrace();
            return;

        }

        // Warten bis Channel geschlossen wurde
        future.getChannel().getCloseFuture().awaitUninterruptibly();
        // Freigabe aller Resourcen
        bootstrap.releaseExternalResources();

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
            port = 8080;
        } else {
            address = args[0];
            port = Integer.parseInt(args[1]);
        }
        new EchoClient(address, port).start();
    }

}