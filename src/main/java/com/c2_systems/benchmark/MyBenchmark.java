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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1000, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)

public class MyBenchmark {

    private Worker worker;

	public interface Worker {
        void work(int threads);
    }

	private int intenseCalculation(int i) {
		Double d = Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(Math.atan(123456789.123456789d))))))))));
		return d.intValue() + i;
	}

	private int semiIntenseCalculation(int i) {
		Double d = Math.tan(Math.atan(Math.tan(Math.atan(Math.tan(123456789.123456789d)))));
		return d.intValue() + i;
	}

	private int nonIntenseCalculation(int i) {
		Double d = Math.tan(Math.atan(123456789.123456789d));
		return d.intValue() + i;
	}

    @Setup
    public void setup(final Blackhole bh) {

    	worker = new Worker() {

        	@Override
        	public void work(int threads) {

				int threadCt = Runtime.getRuntime().availableProcessors() + 1;

				ExecutorService executor = Executors.newFixedThreadPool(threadCt);
				Scheduler scheduler = Schedulers.from(executor);

				Observable.range(1,1000).flatMap(i -> Observable.just(i)
					    .subscribeOn(scheduler)
					    .map(i2 -> intenseCalculation(i2))
					).doAfterTerminate(() -> executor.shutdown())
					.subscribe(bh::consume);


        	}

        };

    }

    @Benchmark
    public void threads___1() {
        worker.work(1);
    }
    @Benchmark
    public void threads___2() {
        worker.work(2);
    }
    @Benchmark
    public void threads___3() {
        worker.work(3);
    }
    @Benchmark
    public void threads___4() {
        worker.work(4);
    }
    @Benchmark
    public void threads___5() {
        worker.work(5);
    }
    @Benchmark
    public void threads___6() {
        worker.work(6);
    }
    @Benchmark
    public void threads___7() {
        worker.work(7);
    }
    @Benchmark
    public void threads___8() {
        worker.work(8);
    }
    @Benchmark
    public void threads___9() {
        worker.work(9);
    }
    @Benchmark
    public void threads__10() {
        worker.work(10);
    }
    @Benchmark
    public void threads__11() {
        worker.work(11);
    }
    @Benchmark
    public void threads__12() {
        worker.work(12);
    }
    @Benchmark
    public void threads__13() {
        worker.work(13);
    }
    @Benchmark
    public void threads__14() {
        worker.work(14);
    }
    @Benchmark
    public void threads__15() {
        worker.work(15);
    }
    @Benchmark
    public void threads__16() {
        worker.work(16);
    }
    @Benchmark
    public void threads__17() {
        worker.work(17);
    }
    @Benchmark
    public void threads__18() {
        worker.work(18);
    }
    @Benchmark
    public void threads__19() {
        worker.work(19);
    }
    @Benchmark
    public void threads__20() {
        worker.work(20);
    }

}
