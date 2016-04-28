package nl.rddesmit;

import akka.actor.Props;

/**
 * Reply with a 'Token' to every message
 *
 * Created by rudies on 26-4-2016.
 */
class TokenActor extends RandomErrorActor {

    static Props props(){
        return Props.create(TokenActor.class);
    }

    private TokenActor(){
        super();
    }

    @Override
    public void onReceiveMsg(Object message) {
        sender().tell(new Token(), self());
    }

    //messages
    class Token{

    }
}
