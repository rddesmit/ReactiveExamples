package nl.rddesmit;

import java.util.Random;

/**
 * Throw a exception or delegate the message to the 'onReceiveMsg' method with a fifty-fifty change.
 *
 * Created by rudies on 25-4-2016.
 */
abstract class RandomErrorActor extends PeekMailBoxActor {

    private final Random random = new Random();

    @Override
    public final void onReceive(Object message) throws Exception {
        if(random.nextBoolean()) {
            onReceiveMsg(message);
            ack(getContext());
        }
        else throw new Exception();
    }

    public abstract void onReceiveMsg(Object message);
}
