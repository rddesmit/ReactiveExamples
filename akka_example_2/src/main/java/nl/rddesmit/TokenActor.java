package nl.rddesmit;

import akka.actor.Props;

/**
 * Created by rudies on 26-4-2016.
 */
public class TokenActor extends RandomErrorActor {

    public static Props props(){
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
