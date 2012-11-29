package netty.samples;

/**
 * Created with IntelliJ IDEA.
 * User: rsi
 * Date: 11/28/12
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Receiver {
    protected final String expectedMessage;
    protected final long t0;

    public Receiver(String expectedMessage, long t0) {
        this.expectedMessage = expectedMessage;
        this.t0 = t0;
    }

    public abstract void receive(String message);

    @Override
    public String toString() {
        return "Receiver{" +
                "expectedMessage='" + expectedMessage + '\'' +
                ", t0=" + t0 +
                '}';
    }
}
