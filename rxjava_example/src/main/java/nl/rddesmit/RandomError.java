package nl.rddesmit;

import rx.Observable;
import rx.Subscriber;

import java.util.Random;

/**
 * Observable source that will produce the single given value or a error. There is a fifty-fifty change that the
 * error will be produced.
 * @param <T>
 *
 * Created by rudies on 25-4-2016.
 */
class RandomError<T> implements Observable.OnSubscribe<T> {

    private final Random random = new Random();
    private final T value;

    <R extends T> RandomError(R value){
        this.value = value;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        try {
            if (random.nextBoolean()) {
                subscriber.onNext(value);
                subscriber.onCompleted();
            }
            else throw new Exception();
        } catch (Exception e){
            subscriber.onError(e);
        }
    }
}
