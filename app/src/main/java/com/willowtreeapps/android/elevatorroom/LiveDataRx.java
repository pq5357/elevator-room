package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by willowtree on 5/4/17.
 * <p>
 * an extension to LiveDataReactiveStreams
 */

public class LiveDataRx {

    private LiveDataRx() {
    }

    /**
     * Creates an Observable {@link LiveData} stream from an rx publisher that will never complete.
     * <p>
     * use LiveDataReactiveStreams for completable publishers
     */
    public static <T> LiveData<T> fromEternalPublisher(final Publisher<T> publisher) {
        return new ActiveOnlyPublisherLiveData<>(publisher);
    }

    /**
     * LiveData that only subscribes to the publisher when active
     */
    private static class ActiveOnlyPublisherLiveData<T> extends LiveData<T> {

        private final Publisher<T> publisher;
        private Subscription subscription;
        private final Subscriber<T> subscriber = new Subscriber<T>() {

            @Override
            public void onSubscribe(Subscription s) {
                if (subscription != null) {
                    subscription.cancel(); // make sure old subscription is cancelled
                }
                subscription = s;
                // Don't worry about backpressure. If the stream is too noisy then backpressure can
                // be handled upstream.
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(final T t) {
                postValue(t);
            }

            @Override
            public void onError(Throwable t) {
                // Errors should be handled upstream, so propagate as a crash.
                throw new RuntimeException(t);
            }

            @Override
            public void onComplete() {
            }
        };

        public ActiveOnlyPublisherLiveData(Publisher<T> publisher) {
            this.publisher = publisher;
        }

        @Override
        protected void onActive() {
            super.onActive();
            publisher.subscribe(subscriber);
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            if (subscription != null) {
                subscription.cancel();
            }
        }

    }

}
