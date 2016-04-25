package nl.rddesmit;

import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Created by rudies on 25-4-2016.
 */
class TokenActor extends RandomErrorActor{

    static Props props(){
        return Props.create(TokenActor.class);
    }

    @Override
    public void onReceiveMsg(Object message) {
        getSender().tell(new Token(), getSelf());
    }

    //messages
    class Token {

    }
}
