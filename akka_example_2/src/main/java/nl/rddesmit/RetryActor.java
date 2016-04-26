package nl.rddesmit;

import akka.actor.*;
import akka.dispatch.OnSuccess;
import akka.japi.pf.DeciderBuilder;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.pattern.Patterns.ask;

import akka.japi.pf.ReceiveBuilder;
import scala.Function1;
import scala.runtime.BoxedUnit;

import java.util.*;

import static akka.actor.SupervisorStrategy.resume;

/**
 * Created by rudies on 26-4-2016.
 */
class RetryActor extends AbstractActor {

    private RetryQueue retryQueue = new RetryQueue();

    static Props props(){
        return Props.create(RetryActor.class);
    }

    private RetryActor() {
        receive(ReceiveBuilder
                .match(Try.class, message -> {
                    final ActorRef to = context().actorOf(message.to);
                    final Retry retry = new Retry(to, sender(), message.retries, message.message);

                    // tell self to retry the message
                    self().tell(retry, self());
                })
                .match(Retry.class, message -> {
                    final Retry retry = message.copy();

                    // save the message to the queue and try to execute the request
                    retryQueue.add(retry);
                    ask(retry.to, retry.message, 100000).onSuccess(new PropagateResult(retry), context().dispatcher());
                })
                .matchAny(this::unhandled)
                .build());
    }

    @Override
    public SupervisorStrategy supervisorStrategy(){
        return new OneForOneStrategy(DeciderBuilder
                .matchAny(o -> {
                    // get the last message
                    final Retry retry = retryQueue.poll(sender());

                    // retry the request until no more retries are available
                    if(retry.retries >= 0){
                        self().tell(retry, self());
                        return resume();
                    } else return escalate();
                })
                .build());
    }

    private static class Retry{
        private final ActorRef to;
        private final ActorRef from;
        private final int retries;
        private final Object message;

        private Retry(final ActorRef to, final ActorRef from, final int retries, final Object message){
            this.to = to;
            this.from = from;
            this.retries = retries;
            this.message = message;
        }

        private Retry copy(){
            return new Retry(to, from, retries - 1, message);
        }
    }

    static class Try{
        private final Props to;
        private final int retries;
        private final Object message;

        Try(final Props to, final int retries, final Object message){
            this.to = to;
            this.retries = retries;
            this.message = message;
        }
    }

    private class RetryQueue{
        private final Map<ActorRef, Queue<Retry>> queue = new HashMap<>();

        private void createQueueIfNotExist(ActorRef key){
            if (!queue.containsKey(key)) {
                queue.put(key, new PriorityQueue<>());
            }
        }

        /**
         * Add a retry message to the que of the receiving actor. Creates a new queue when necessary.
         */
        private void add(Retry retry){
            createQueueIfNotExist(retry.to);
            queue.get(retry.to).add(retry);
        }

        /**
         * Retrieve the first known message send to this actor.
         */
        private Retry poll(ActorRef sender){
            return queue.get(sender).poll();
        }
    }

    private class PropagateResult extends OnSuccess<Object>{

        private final Retry retry;

        private PropagateResult(Retry retry) {
            this.retry = retry;
        }

        @Override
        public void onSuccess(Object result) throws Throwable {
            retryQueue.poll(retry.to);
            retry.from.tell(result, retry.to);
        }
    }
}
