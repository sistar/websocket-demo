package netty.samples;

import com.google.common.collect.Multiset;

import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/28/12
 * Time: 4:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimerReceiver extends Receiver {

    private final CountDownLatch responseCountDownLatch;
    private final Multiset<Long> elapsedSet;
    private final WebSocketClientRaw2 webSocketClientRaw2;

    public TimerReceiver(String expectedMessage, long t0, final CountDownLatch responseCountDownLatch, Multiset<Long> elapsedSet, WebSocketClientRaw2 webSocketClientRaw2) {
        super(expectedMessage, t0);
        this.responseCountDownLatch = responseCountDownLatch;
        this.elapsedSet = elapsedSet;
        this.webSocketClientRaw2 = webSocketClientRaw2;
    }

    @Override

    public void receive(String message) {
        final long elapsed = System.currentTimeMillis() - t0;
        webSocketClientRaw2.getChannelHandler().removeFinishedCallback(this,message,elapsed,responseCountDownLatch,elapsedSet);


    }

}
