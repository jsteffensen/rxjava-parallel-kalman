package com.c2_systems.benchmark;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)

public class MyBenchmark {

    private Worker workerSequential;
    private Worker workerParallel;

	private int semiIntenseCalculation(int i) {
		Double d = Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(i)))))))))))))))));
		return d.intValue() + i;
	}

	private int nonIntenseCalculation(int i) {
		Double d = Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.tan(Math.atan(i)))))));
		return d.intValue() + i;
	}


	private Observable<Object> intensiveObservable() {
	    return Observable.fromCallable(new Callable<Object>() {
	        @Override
	        public Object call() throws Exception {

	        	int randomNumforSemi = ThreadLocalRandom.current().nextInt(0, 101);
	        	Integer i =  semiIntenseCalculation(randomNumforSemi);
	        	int randomNumforNon = ThreadLocalRandom.current().nextInt(0, 101);
	        	Integer j = nonIntenseCalculation(randomNumforNon);

				return i+j;
	        }
	    });
	};

	private Observable<Object> semiIntensiveObservable() {
	    return Observable.fromCallable(new Callable<Object>() {
	        @Override
	        public Object call() throws Exception {
	        	int randomNumforSemi = ThreadLocalRandom.current().nextInt(0, 101);
	        	return semiIntenseCalculation(randomNumforSemi);
	        }
	    });
	};

	private Observable<Object> nonIntensiveObservable() {
	    return Observable.fromCallable(new Callable<Object>() {
	        @Override
	        public Object call() throws Exception {
	        	int randomNumforNon = ThreadLocalRandom.current().nextInt(0, 101);
	        	return nonIntenseCalculation(randomNumforNon);
	        }
	    });
	};

	public interface Worker {
        void work();
    }

    @Setup
    public void setup(final Blackhole bh) {

    	workerSequential = new Worker() {

			@Override
        	public void work() {


				Observable.just(intensiveObservable())
		          .subscribe(new Subscriber<Object>() {

			        @Override
			        public void onError(Throwable error) {

			        }

			        @Override
			        public void onCompleted() {

			        }

					@Override
					public void onNext(Object arg) {
						bh.consume(arg);

					}
			    });
        	}
    	};

    	workerParallel = new Worker() {
        	@Override
        	public void work() {
        		Observable.zip(semiIntensiveObservable().subscribeOn(Schedulers.computation()),
        					   nonIntensiveObservable().subscribeOn(Schedulers.computation()),
        					   new Func2<Object, Object, Object>() {

					@Override
					public Object call(Object semiIntensive, Object nonIntensive) {
		    			return (Integer)semiIntensive + (Integer)nonIntensive;
					}

				}).subscribe(bh::consume);
        	}
        };

    }

    @Benchmark
    public void calculateSequential() {
        workerSequential.work();
    }

    @Benchmark
    public void calculateParallel() {
        workerParallel.work();
    }
}