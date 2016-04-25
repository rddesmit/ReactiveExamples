package nl.rddesmit;

import akka.actor.UntypedActor;
import akka.japi.pf.FI;
import akka.japi.pf.ReceiveBuilder;

import java.util.Random;

/**
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
