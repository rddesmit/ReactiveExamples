package nl.rddesmit;

import akka.actor.*;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;

import static akka.pattern.Patterns.ask;
import static akka.actor.SupervisorStrategy.resume;

import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import scala.concurrent.Future;

/**
 * Created by rudies on 26-4-2016.
 */
class RetryActor extends AbstractActor {

    static Props props(){
        return Props.create(RetryActor.class);
    }

    private RetryActor() {
        receive(ReceiveBuilder
                // tell self to retry the message
                .match(Try.class, message -> {
                    self().tell(message.retry(sender()), self());
                })
                // try to send te message
                .match(Retry.class, message -> {
                    Future<Object> result = ask(message.to, message.message, message.timeout);
                    result.onSuccess(new ReturnOnSuccess(message), context().dispatcher());
                    result.onFailure(new RetryOnFailure(message), context().dispatcher());
                })
                .matchAny(this::unhandled)
                .build());
    }

    private static class Retry{
        private final ActorRef to;
        private final ActorRef from;
        private final Object message;
        private final int retries;
        private final Timeout timeout;

        private Retry(final ActorRef to, final ActorRef from, final Object message, final int retries, final Timeout timeout){
            this.to = to;
            this.from = from;
            this.message = message;
            this.retries = retries;
            this.timeout = timeout;
        }

        private Retry retry(){
            return new Retry(to, from, message, retries - 1, timeout);
        }
    }

    static class Try{
        private final ActorRef to;
        private final Object message;
        private final int retries;
        private final Timeout timeout;

        Try(final ActorRef to, final Object message, final int retries, final Timeout timeout){
            this.to = to;
            this.message = message;
            this.retries = retries;
            this.timeout = timeout;
        }

        private Retry retry(ActorRef sender){
            return new Retry(to, sender, message, retries, timeout);
        }
    }

    /**
     * Send the result to the original sender
     */
    private class ReturnOnSuccess extends OnSuccess<Object>{

        private final Retry retry;

        private ReturnOnSuccess(final Retry retry) {
            this.retry = retry;
        }

        @Override
        public void onSuccess(Object result) throws Throwable {
            retry.from.tell(result, retry.to);
        }
    }

    /**
     * If the original message has 'retries' left send the retry message to 'self()' with one less retry.
     */
    private class RetryOnFailure extends OnFailure{

        private final Retry retry;

        private RetryOnFailure(final Retry retry) {
            this.retry = retry;
        }

        @Override
        public void onFailure(Throwable failure) throws Throwable {
            if(retry.retries < 0) throw new RuntimeException("Retries exceeded", failure);

            self().tell(retry.retry(), self());
        }
    }
}
