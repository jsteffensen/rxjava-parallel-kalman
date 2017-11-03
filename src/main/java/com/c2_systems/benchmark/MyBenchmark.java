package com.c2_systems.benchmark;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
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
//@Fork(value = 1,jvmArgsAppend = { "-XX:MaxInlineLevel=20" })
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class MyBenchmark implements Function<Integer, Integer> {

    @Param({"2000", "4000", "6000"})
    public int count;

    @Param({"50", "100", "250", "500", "1000"})
    public int compute;

    @Param({"3"})
    public int parallelism;

    Integer[] ints;
    Integer[] moreints;

    Random randomNum = new Random();

    KalmanFilter kfilter;
    RealVector u;
    RealVector x;
    RealMatrix A;
    RealMatrix B;
    RealMatrix H;
    double dt;
    double measurementNoise;
    double accelNoise;

    Flowable<Integer> parallel;
    Flowable<Integer> notsoparallel;
    Flowable<Integer> zippedparallel;

    @Override
    public Integer apply(Integer t) throws Exception {

    	RandomGenerator rand = new JDKRandomGenerator();
        RealVector tmpPNoise = new ArrayRealVector(new double[] { Math.pow(dt, 2d) / 2d, dt });
        RealVector mNoise = new ArrayRealVector(1);

        kfilter.predict(u);
        RealVector pNoise = tmpPNoise.mapMultiply(accelNoise * rand.nextGaussian());
        x = A.operate(x).add(B.operate(u)).add(pNoise);
        mNoise.setEntry(0, measurementNoise * rand.nextGaussian());
        RealVector z = H.operate(x).add(mNoise);

        kfilter.correct(z);

        Double position = kfilter.getStateEstimation()[0];
        Double velocity = kfilter.getStateEstimation()[1];

        return velocity.intValue();
    }

    @Setup
    public void setup() {

        final int cpu = parallelism;
        ints = new Integer[count];
        moreints = new Integer[count];
        //Arrays.fill(ints, 777);

        for(int i = 0; i<count; i++) {
        	ints[i] = randomNum.nextInt(2);
        }
        for(int i = 0; i<count; i++) {
        	moreints[i] = randomNum.nextInt(2);
        }

        Flowable<Integer> source = Flowable.fromArray(ints);
        Flowable<Integer> anothersource = Flowable.fromArray(moreints);


        dt = 0.1d;
        measurementNoise = 10d;
        accelNoise = 0.2d;

        A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });
        B = new Array2DRowRealMatrix(new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });
        H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });
        x = new ArrayRealVector(new double[] { 0, 0 });

        RealMatrix tmp = new Array2DRowRealMatrix(new double[][] {
            { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
            { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });

        RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));

        RealMatrix P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });

        RealMatrix R = new Array2DRowRealMatrix(new double[] { Math.pow(measurementNoise, 2) });

        u = new ArrayRealVector(new double[] { 0.1d });

        ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        kfilter = new KalmanFilter(pm, mm);



        parallel = source.parallel(cpu).runOn(Schedulers.computation()).map(this).sequential();

        notsoparallel = source.map(this);

        zippedparallel = Flowable.zip(source, anothersource,
                new BiFunction<Integer, Integer, Integer>() {

					@Override
					public Integer apply(Integer sourceInteger, Integer anothersourceInteger) throws Exception {
						return sourceInteger + anothersourceInteger;
					}

        }).parallel(cpu).runOn(Schedulers.computation()).map(this).sequential();


    }


    void subscribe(Flowable<Integer> f, Blackhole bh) {
        PerfAsyncConsumer consumer = new PerfAsyncConsumer(bh);
        f.subscribe(consumer);
        consumer.await(count);
    }

    public void other(Blackhole bh) {
    	for(int i = 0; i<count; i++) {
			Blackhole.consumeCPU(compute);
		}
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
    		for(int i =0; i<ints.length; i++) {
        		executor.submit(() -> {
        			Blackhole.consumeCPU(compute);
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

		for(int i =0; i<ints.length; i++) {
    		Blackhole.consumeCPU(compute);
    	}

    }
}