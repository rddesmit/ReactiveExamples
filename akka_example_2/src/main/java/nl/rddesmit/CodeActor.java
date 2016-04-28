package nl.rddesmit;

import akka.actor.Props;

/**
 * Reply with a 'Code' to every message
 *
 * Created by rudies on 26-4-2016.
 */
class CodeActor extends RandomErrorActor {

    static Props props(){
        return Props.create(CodeActor.class);
    }

    private CodeActor() {
        super();
    }

    @Override
    public void onReceiveMsg(Object message) {
        sender().tell(new Code(), self());
    }

    //messages
    class Code {

    }
}
