package de.opitz_consulting.demo.websocket;


import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String WEBSOCKET_PATH = "/wss";
    private final ChannelGroup wsGroup;

    public WebSocketServerHandler(ChannelGroup wsGroup) {
        this.wsGroup = wsGroup;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.wsGroup.remove(e.getChannel());
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            log.info("http req {} {} {}", ((HttpRequest) msg).getUri(), ((HttpRequest) msg).getMethod(), ((HttpRequest) msg).getHeaders());
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else {

            final Channel senderChannel = ctx.getChannel();


            if (msg instanceof WebSocketFrame) {
                log.info("ws req {} ", msg);
                WebSocketFrame frame = (WebSocketFrame) msg;
                if (frame instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                    System.out.println("WebSocket Client received message: " + textFrame.getText());
                    log.debug("sending {} to {}", msg, senderChannel);
                    senderChannel.write(msg);
                } else if (frame instanceof PongWebSocketFrame) {
                    System.out.println("WebSocket Client received pong");
                } else if (frame instanceof CloseWebSocketFrame) {
                    System.out.println("WebSocket Client received closing");
                    senderChannel.close();
                }
                /*for (Channel channel : wsGroup) {
                    if(! channel.equals(senderChannel))  {
                        channel.write(msg);
                    }
                }*/
                // will not work because of sending to myself :
                //wsGroup.write(msg);

            } else {
                // Ungueltige Nachricht, somit schliessen des Channel's
                log.error("INVALID req {}", msg);
                ctx.getChannel().close();
            }
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        // Ueberpruefen ob der Request ein GET ist oder nicht, wenn nicht 
        // kann dieser nicht bearbeitet werden. Somit senden eines 403 Status-Code‘s
        if (req.getMethod() != HttpMethod.GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

        final String uri = req.getUri();
        if (uri.startsWith("/chat")) {
            // Handshake
            log.info("HANDSHAKE");
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, false);          //"echo-protocol"
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            // Ueberpruefen ob ein geeigneter WebSocketServerHandshaker fuer den Request
            // gefunden worden konnte. Wenn nicht wird der Client darueber informiert
            if (handshaker == null) {
                log.error("NO HANDSHAKER FOUND");
                wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            } else {
                // fuehre den Handshake

                handshaker.handshake(ctx.getChannel(), req).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            // Handshake war erfolgreich. Fuege Channel in die 
                            // ChannelGroup hinzu um so auch UDP Nachrichten
                            // zu Empfangen
                            wsGroup.add(future.getChannel());
                        } else {
                            // Handshake hat nicht geklappt. Feuere einen
                            // exceptionCaught event
                            log.error("NO HANDSHAKE FAILED {} ", ctx.getChannel());
                            Channels.fireExceptionCaught(future.getChannel(), future.getCause());
                        }
                    }
                });
            }
        } else {
            log.info("SEND 404");
            // Sende ein 404
            HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            sendHttpResponse(ctx, req, res);

        }


    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Erzeugen einer “Error-Page” wenn Status-Code nicht OK (200) ist.
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            HttpHeaders.setContentLength(res, res.getContent().readableBytes());
        }

        // Senden der HttpResponse
        ChannelFuture f = ctx.getChannel().write(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().getCode() != 200) {
            // Galls der HttpRequest nicht den Keep-Alive Geader enthielt
            // oder der Status-Code nicht 200 war wird der Channel nach dem 
            // Senden der Nachricht geschlossen
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // Stacktrace nach STDOUT ausgeben und Channel schliessen
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private static String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }
}

