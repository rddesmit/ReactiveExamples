package nl.rddesmit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.ask;

/**
 * Retry via mailbox
 *
 * Created by rudies on 25-4-2016.
 */
public class Main {

    // Create the actor system and load the config. The peek-dispatcher is applied to the 'code' and 'token' actor
    private static final ActorSystem actorSystem = ActorSystem.create("ActorSystem", ConfigFactory.load());
    private static final ActorRef codeActor = actorSystem.actorOf(CodeActor.props(), "code");
    private static final ActorRef tokenActor = actorSystem.actorOf(TokenActor.props(), "token");

    public static void main(String[] args){
        // Trade a url for a code and a code for a token
        Future<Object> future = future(() -> "url", actorSystem.dispatcher())
                .flatMap(new GetCode(), actorSystem.dispatcher())
                .flatMap(new GetToken(), actorSystem.dispatcher());

        // Print the result
        future.onSuccess(new PrintLnOnSuccess(), actorSystem.dispatcher());
        future.onFailure(new PrintLnOnFailure(), actorSystem.dispatcher());
    }

    /**
     * Try three times and within 10 seconds to create a code.
     */
    private static class GetCode extends Mapper<String, Future<Object>>{

        @Override
        public Future<Object> apply(final String actor){
            return ask(codeActor, "", Timeout.apply(10, TimeUnit.SECONDS));
        }
    }

    /**
     * Try three times and within 500 ms to create a token.
     */
    private static class GetToken extends Mapper<Object, Future<Object>>{

        @Override
        public Future<Object> apply(Object object){
            return ask(tokenActor, "", Timeout.apply(500, TimeUnit.MILLISECONDS));
        }
    }

    private static class PrintLnOnSuccess extends OnSuccess<Object>{

        @Override
        public void onSuccess(Object result) throws Throwable {
            System.out.println(result);
        }
    }

    private static class PrintLnOnFailure extends OnFailure{

        @Override
        public void onFailure(Throwable failure) throws Throwable {
            System.out.println(failure);
        }
    }
}
