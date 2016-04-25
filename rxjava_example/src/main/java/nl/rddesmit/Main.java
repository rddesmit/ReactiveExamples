package nl.rddesmit;

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
}
