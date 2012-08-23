package me.normanmaurer.javamagazin.netty.examples.echo;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * 
 * @author Norman Maurer <norman@apache.org>
 *
 */
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() {
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new EchoServerHandler());
            }
        });

        // Bind die Lokale Adresse
        bootstrap.bind(new InetSocketAddress(port));
    }

    class EchoServerHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            System.out.println("RECV from Client: " + ChannelBuffers.hexDump(
                    (ChannelBuffer) e.getMessage()));

            // Sende empfangende Nachricht wieder zurück zum Client
            e.getChannel().write(e.getMessage());

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getChannel().close();
        }

    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length < 1) {
            port = 8080;
        } else {
            port = Integer.parseInt(args[0]);
        }
        new EchoServer(port).start();

    }

}