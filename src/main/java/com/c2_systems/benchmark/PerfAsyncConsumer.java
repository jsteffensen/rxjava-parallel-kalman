package com.c2_systems.benchmark;

import java.util.concurrent.*;

import org.openjdk.jmh.infra.Blackhole;
import org.reactivestreams.*;

import io.reactivex.CompletableObserver;
import io.reactivex.FlowableSubscriber;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

/**
 * A multi-type asynchronous consumer.
 */
public final class PerfAsyncConsumer extends CountDownLatch implements FlowableSubscriber<Object>, Observer<Object>,
SingleObserver<Object>, CompletableObserver, MaybeObserver<Object> {

    final Blackhole bh;

    public PerfAsyncConsumer(Blackhole bh) {
        super(1);
        this.bh = bh;
    }

    @Override
    public void onSuccess(Object value) {
        bh.consume(value);
        countDown();
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Object t) {
        bh.consume(t);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        countDown();
    }

    @Override
    public void onComplete() {
        bh.consume(true);
        countDown();
    }

    /**
     * Wait for the terminal signal.
     * @param count if less than 1001, a spin-wait is used
     */
    public void await(int count) {
        if (count <= 1000) {
            while (getCount() != 0) { }
        } else {
            try {
                await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}