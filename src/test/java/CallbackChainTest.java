import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/28/12
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class CallbackChainTest {
    @Test
    public void testChainedCallback() throws Exception {


        final Receiver receiver = new Receiver("x");

        final ExternalCallback externalCallback = new ExternalCallback();
        externalCallback.registerReceiver(receiver);
        assertThat(externalCallback.receivers.size(),is(equalTo(1)));
        externalCallback.onMessageReceived("x");
        assertThat(externalCallback.receivers.size(),is(equalTo(0)));
    }

    private class ExternalCallback {
        final Set<Receiver> receivers = new CopyOnWriteArraySet<Receiver>();

        public void registerReceiver(Receiver receiver) {
            receivers.add(receiver);
        }

        public void onMessageReceived(String message) {
            for (Receiver receiver : receivers) {
                if(receiver.receive(message)){
                  receivers.remove(receiver);
                }
            }
        }
    }


    private class Receiver {

        private final String expected;

        private Receiver(String expected) {
            this.expected = expected;
        }

        public boolean receive(String message) {
            return this.expected.equals(message);
        }
    }
}
