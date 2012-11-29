package netty.samples;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class SimpleCallbackRawTest {

    Logger log = LoggerFactory.getLogger(this.getClass());
    //final String host = "ec2-46-137-59-250.eu-west-1.compute.amazonaws.com";
    final String host = "localhost";
    //final String port = "8080";
    final String port = "8443";
    @Test
    public void massTest() throws Exception {



        final URI uri = new URI("ws://" + host + ":" + port + "/chat");

        final ClientPool clientPool = new ClientPool(uri);
        clientPool.generateClients(40);
        clientPool.sendTestMessagesToRandomClients(20);

        System.out.println(clientPool.elapsedSet);
    }

        @Test
        public void testThatOnlyTheGivenCallBackIsCalledBack() throws Exception {


            final URI uri = new URI("ws://" + host + ":" + port + "/chat");

            final ClientPool clientPool = new ClientPool(uri);

            final WebSocketClientRaw2 client = clientPool.createClient();
            final WebSocketClientRaw2 client1 = clientPool.createClient();

            final CountDownLatch latch = new CountDownLatch(1);
            client.sendMessage("a", latch, clientPool.elapsedSet);

            final CountDownLatch latch1 = new CountDownLatch(1);
            client1.sendMessage("x", latch1, clientPool.elapsedSet);



            latch.await(2, TimeUnit.SECONDS);
            latch1.await(2,TimeUnit.SECONDS);
            //clientPool.close();

            System.out.println(clientPool.elapsedSet);
            //clientPool.exit();









        /*
final AtomicInteger responseCounterA = new AtomicInteger();
final AtomicInteger responseCounterB = new AtomicInteger();

final String host = "ec2-46-137-59-250.eu-west-1.compute.amazonaws.com";

a.connect().awaitUninterruptibly();
b.connect().awaitUninterruptibly();
a.send(new DefaultWebSocketFrame("to A"));
Thread.sleep(4000L);
assertThat(responseCounterA.get(), is(equalTo(1)));

        */
    }
}
