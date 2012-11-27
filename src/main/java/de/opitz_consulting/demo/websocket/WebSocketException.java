package de.opitz_consulting.demo.websocket;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/6/12
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebSocketException extends IOException {
    public WebSocketException(String s, Throwable t) {
        super(s,t);
    }
    public WebSocketException(String s) {
        super(s);
    }
}
