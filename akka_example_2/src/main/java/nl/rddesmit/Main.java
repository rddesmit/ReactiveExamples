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
import static akka.pattern.Patterns.ask;

public class Main {

    private static final ActorSystem actorSystem = ActorSystem.create();
    private static final ActorRef retryActor = actorSystem.actorOf(RetryActor.props());

    public static void main(String[] args) {
        Future<Object> future = future(() -> "url", actorSystem.dispatcher())
                .flatMap(new GetCode(), actorSystem.dispatcher())
                .flatMap(new GetToken(), actorSystem.dispatcher());

        future.onSuccess(new PrintLnOnSucces(), actorSystem.dispatcher());
        future.onFailure(new PrintLnOnFailure(), actorSystem.dispatcher());

    }

    private static class GetCode extends Mapper<String, Future<Object>> {

        @Override
        public Future<Object> apply(final String url) {
            return ask(retryActor, new RetryActor.Try(CodeActor.props(), 3, ""), Timeout.apply(1, TimeUnit.HOURS));
        }
    }

    private static class GetToken extends Mapper<Object, Future<Object>> {

        @Override
        public Future<Object> apply(final Object object) {
            return ask(retryActor, new RetryActor.Try(TokenActor.props(), 3, ""), Timeout.apply(500, TimeUnit.MILLISECONDS));
        }
    }

    private static class PrintLnOnSucces extends OnSuccess<Object> {

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