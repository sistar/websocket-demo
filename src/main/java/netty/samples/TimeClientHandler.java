package netty.samples;

import org.jboss.netty.channel.*;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/28/12
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeClientHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        System.out.println(e.getMessage());
    }

}
