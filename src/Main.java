import rx.Observable;
import rx.Subscriber;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        //try three times to create a code
        Observable<Integer> codeObservable = Observable.create(new Code())
                .retry(3);

        //try three times and within 500 ms to create a token
        Observable<Integer> tokenObservable = Observable.create(new Token())
                .retry(3)
                .timeout(500, TimeUnit.MILLISECONDS);

        // trade a url for a code, and a code for a token
        Observable.just("url")
                .flatMap(url -> codeObservable)
                .flatMap(code -> tokenObservable)
                .subscribe(System.out::println, System.out::println);
    }

    public static class RandomError<T> implements Observable.OnSubscribe<T> {

        private final T value;

        public <R extends T> RandomError(R value){
            this.value = value;
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            try {
                if (new Random().nextBoolean()) subscriber.onNext(value);
                else throw new Exception();
            } catch (Exception e){
                subscriber.onError(e);
            }
        }
    }

    public static class Code extends RandomError<Integer>{
        public Code(){
            super(1);
        }
    }

    public static class Token extends RandomError<Integer>{
        public Token(){
            super(2);
        }
    }
}
