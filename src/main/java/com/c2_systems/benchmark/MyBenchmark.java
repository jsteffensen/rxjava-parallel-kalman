/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.c2_systems.benchmark;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

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
import rx.Scheduler;
import rx.schedulers.Schedulers;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(4)
@State(Scope.Thread)

public class MyBenchmark {

    private Worker workerOne;
    private Worker workerTwo;

	public interface Worker {
        void work();
    }

	private int intenseCalculation(int i) {
		Double d = Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(123456789.123456789d))))))))));
		return d.intValue() + i;
	}

    @Setup
    public void setup(final Blackhole bh) {

    	workerOne = new Worker() {

			@Override
        	public void work() {

				int threadCt = 1;

				ExecutorService executor = Executors.newFixedThreadPool(threadCt);
				Scheduler scheduler = Schedulers.from(executor);

				Observable.range(1,1000).flatMap(i -> Observable.just(i)
					    .subscribeOn(scheduler)
					    .map(i2 -> intenseCalculation(i2))
					).doAfterTerminate(() -> executor.shutdown())
					.subscribe();


        	}
    	};

    	workerTwo = new Worker() {

        	@Override
        	public void work() {

				int threadCt = Runtime.getRuntime().availableProcessors() + 1;

				ExecutorService executor = Executors.newFixedThreadPool(threadCt);
				Scheduler scheduler = Schedulers.from(executor);

				Observable.range(1,1000).flatMap(i -> Observable.just(i)
					    .subscribeOn(scheduler)
					    .map(i2 -> intenseCalculation(i2))
					).doAfterTerminate(() -> executor.shutdown())
					.subscribe();


        	}

        };

    }

    @Benchmark
    public void oneThread() {
        workerOne.work();
    }

    @Benchmark
    public void moreThreads() {
        workerTwo.work();
    }


}
