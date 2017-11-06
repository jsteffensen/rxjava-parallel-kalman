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
@Fork(value = 0)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class MyBenchmark implements Function<FilterObject, FilterObject> {

    @Param({"2000", "4000", "6000"})
    public int count;

    @Param({"50", "100", "250", "500", "1000"})
    public int compute;

    @Param({"4"})
    public int parallelism;

    FilterObject[] filters;
    Integer[] ints;

    Random randomNum = new Random();

    Flowable<FilterObject> parallel;
    Flowable<FilterObject> notsoparallel;
    Flowable<FilterObject> zippedparallel;

    @Override
	public FilterObject apply(FilterObject f) throws Exception {

		f.correct();
		f.predict();
		return f;

	}

    @Setup
    public void setup() {

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

    @Benchmark
    public void zippedparallel(Blackhole bh) {
        subscribe(zippedparallel, bh);
    }


    //@Benchmark
    public void oldschool(Blackhole bh) {

    	ExecutorService executor = Executors.newFixedThreadPool(parallelism);

    	try {
    		for(int i = 0; i<count; i++) {
        		executor.submit(() -> {

        		});
        	}
    	} catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
    		executor.shutdown();
    	}

    }

}