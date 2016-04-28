package nl.rddesmit;

import akka.actor.AbstractActor;
import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.Random;

/**
 * Throw a exception or delegate the message to the 'onReceiveMsg' method with a fifty-fifty change.
 *
 * Created by rudies on 26-4-2016.
 */
abstract class RandomErrorActor extends AbstractActor {

    private final Random random = new Random();

    RandomErrorActor(){
        receive(ReceiveBuilder
            .matchAny(message -> {
                if(random.nextBoolean()) onReceiveMsg(message);
                else throw new Exception();
            }).build());
    }

    public abstract void onReceiveMsg(Object message);
}
