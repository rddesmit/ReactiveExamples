package nl.rddesmit;

import akka.actor.Props;

/**
 * Respond with a 'Token' or a error the a message.
 *
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
