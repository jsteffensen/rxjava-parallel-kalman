package com.c2_systems.benchmark;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class MyBenchmark implements Function<FilterObject, FilterObject> {

    @Param({"500", "1000", "2000"})
    public int count;

    @Param({"8"})
    public int parallelism;

    FilterObject[] filters;
    Integer[] ints;

    Random randomNum = new Random();

    Flowable<FilterObject> parallel;
    Flowable<FilterObject> notsoparallel;
    Flowable<FilterObject> zippedparallel;

    ExecutorService executor;

    @Override
	public FilterObject apply(FilterObject f) {

		f.correct();
		f.predict();

		return f;

	}

    @Setup
    public void setup() {

    	executor = Executors.newFixedThreadPool(parallelism);

        final int cpu = parallelism;
        filters = new FilterObject[count];
        ints = new Integer[count];

        for(int i = 0; i < count; i++) {
        	filters[i] = new FilterObject();
        }
        Arrays.fill(ints, 777);


        Flowable<FilterObject> source = Flowable.fromArray(filters);
        Flowable<Integer> anothersource = Flowable.fromArray(ints);

        parallel = source.parallel(cpu).runOn(Schedulers.computation()).map(this).sequential();

        notsoparallel = source.map(this);

        zippedparallel = Flowable.zip(source, anothersource,
                new BiFunction<FilterObject, Integer, FilterObject>() {

					@Override
					public FilterObject apply(FilterObject arg0, Integer arg1) throws Exception {
						return arg0;
					}

        }).parallel(cpu).runOn(Schedulers.computation()).map(this).sequential();


    }

    void subscribe(Flowable<FilterObject> parallel2, Blackhole bh) {
        PerfAsyncConsumer consumer = new PerfAsyncConsumer(bh);
        parallel2.subscribe(consumer);
        consumer.await(count);
    }

    //@Benchmark
    public void parallel(Blackhole bh) {
        subscribe(parallel, bh);
    }

    //@Benchmark
    public void notsoparallel(Blackhole bh) {
        subscribe(notsoparallel, bh);
    }

    //@Benchmark
    public void zippedparallel(Blackhole bh) {
        subscribe(zippedparallel, bh);
    }


    @Benchmark
    public void oldschool(Blackhole bh) {

    	try {
    		for(int i =0; i<filters.length; i++) {
    			FilterObject f = filters[i];
        		executor.submit(() -> {
        			bh.consume( apply(f) );
        		});
        	}
    	} catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
    		executor.shutdown();
    	}

    }

    //@Benchmark
    public void loopy(Blackhole bh) {

		for(int i =0; i<filters.length; i++) {
			bh.consume( apply(filters[i]) );
    	}
    }

}