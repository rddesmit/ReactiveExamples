package nl.rddesmit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.util.Timeout;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

import static akka.dispatch.Futures.future;
import static nl.rddesmit.Retry.retryJava;

/**
 * Retry via future
 */
public class Main {

    // Create actor system
    private static final ActorSystem actorSystem = ActorSystem.create();
    private static final ActorRef codeActor = actorSystem.actorOf(CodeActor.props());
    private static final ActorRef tokenActor = actorSystem.actorOf(TokenActor.props());

    public static void main(String[] args) {
        // Trade a url for a code and a code for a token
        Future<Object> future = future(() -> "url", actorSystem.dispatcher())
                .flatMap(new GetCode(), actorSystem.dispatcher())
                .flatMap(new GetToken(), actorSystem.dispatcher());

        // Print the result
        future.onSuccess(new PrintLnOnSuccess(), actorSystem.dispatcher());
        future.onFailure(new PrintLnOnFailure(), actorSystem.dispatcher());
    }

    /**
     * Try three times to get the code
     */
    private static class GetCode extends Mapper<String, Future<Object>> {

        @Override
        public Future<Object> apply(final String url) {
            return retryJava(codeActor, "", 3, Timeout.apply(10, TimeUnit.SECONDS), actorSystem.dispatcher());
        }
    }

    /**
     * Try three times to get the token
     */
    private static class GetToken extends Mapper<Object, Future<Object>> {

        @Override
        public Future<Object> apply(final Object object) {
            return retryJava(tokenActor, "", 3, Timeout.apply(500, TimeUnit.MILLISECONDS), actorSystem.dispatcher());
        }
    }

    private static class PrintLnOnSuccess extends OnSuccess<Object> {

        @Override
        public void onSuccess(Object result) throws Throwable {
            System.out.println(result);
        }
    }

    private static class PrintLnOnFailure extends OnFailure {

        @Override
        public void onFailure(Throwable failure) throws Throwable {
            System.out.println(failure);
        }
    }
}