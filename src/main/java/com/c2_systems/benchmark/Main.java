package com.c2_systems.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Main {

	public static void main(String[] args) {
			int cores = Runtime.getRuntime().availableProcessors();
			System.out.println("Running on " + cores + " cores." );
			System.out.println("Thread pool for com.c2_systems.benchmark.MyBenchmark.moreThreads() set to: " + Runtime.getRuntime().availableProcessors() + 1 );
	       Options opt = new OptionsBuilder()
	                .include(MyBenchmark.class.getSimpleName())
	                .forks(1)
	                .build();

	       try {
				new Runner(opt).run();
			} catch (RunnerException e) {
				e.printStackTrace();
			}

	}

}
