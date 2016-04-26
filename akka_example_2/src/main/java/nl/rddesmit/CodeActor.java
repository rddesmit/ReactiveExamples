package nl.rddesmit;

import akka.actor.Props;

/**
 * Created by rudies on 26-4-2016.
 */
public class CodeActor extends RandomErrorActor {

    public static Props props(){
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
