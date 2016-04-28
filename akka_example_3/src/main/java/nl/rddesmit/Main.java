package nl.rddesmit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.dispatch.Recover;
import akka.util.Timeout;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.ask;

public class Main {

    private static final ActorSystem actorSystem = ActorSystem.create();
    private static final ActorRef codeActor = actorSystem.actorOf(CodeActor.props());
    private static final ActorRef tokenActor = actorSystem.actorOf(TokenActor.props());

    public static void main(String[] args) {
        Future<Object> future = future(() -> "url", actorSystem.dispatcher())
                .flatMap(new GetCode(), actorSystem.dispatcher())
                .flatMap(new GetToken(), actorSystem.dispatcher());

        future.onSuccess(new PrintLnOnSuccess(), actorSystem.dispatcher());
        future.onFailure(new PrintLnOnFailure(), actorSystem.dispatcher());
    }

    private static class GetCode extends Mapper<String, Future<Object>> {

        @Override
        public Future<Object> apply(final String url) {
            return Retry.retryJava(codeActor, "", 3, Timeout.apply(10, TimeUnit.SECONDS), actorSystem.dispatcher());
        }
    }

    private static class GetToken extends Mapper<Object, Future<Object>> {

        @Override
        public Future<Object> apply(final Object object) {
            return Retry.retryJava(tokenActor, "", 3, Timeout.apply(500, TimeUnit.MILLISECONDS), actorSystem.dispatcher());
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