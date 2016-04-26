package nl.rddesmit;

import akka.actor.AbstractActor;
import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.Random;

/**
 * Created by rudies on 26-4-2016.
 */
public abstract class RandomErrorActor extends AbstractActor {

    private final Random random = new Random();

    protected RandomErrorActor(){
        receive(ReceiveBuilder
            .matchAny(message -> {
                if(random.nextBoolean()) onReceiveMsg(message);
                else throw new Exception();
            }).build());
    }

    public abstract void onReceiveMsg(Object message);
}
