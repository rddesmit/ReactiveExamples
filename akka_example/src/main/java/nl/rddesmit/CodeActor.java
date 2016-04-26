package nl.rddesmit;

import akka.actor.Props;

/**
 * Respond with a 'Code' or a error to a message.
 *
 * Created by rudies on 25-4-2016.
 */
class CodeActor extends RandomErrorActor{

    static Props props(){
        return Props.create(CodeActor.class);
    }

    @Override
    public void onReceiveMsg(Object message) {
        getSender().tell(new Code(), getSelf());
    }

    //messages
    class Code {

    }
}
