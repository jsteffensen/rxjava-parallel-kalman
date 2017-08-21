import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class Main {
	private static long time1;
	private static long time2;
	private static long time3;
	private static long time4;
	private static long dur1;
	private static long dur2;

	public static void main(String[] args) {

		int threadCt = Runtime.getRuntime().availableProcessors() + 1;
		int threadCt2 = 1;

		System.out.println("Running " + threadCt + " CPU cores.");

		ExecutorService executor = Executors.newFixedThreadPool(threadCt);
		Scheduler scheduler = Schedulers.from(executor);
		ExecutorService executor2 = Executors.newFixedThreadPool(threadCt2);
		Scheduler scheduler2 = Schedulers.from(executor2);

		time1 = System.currentTimeMillis();

		Observable.range(0,999)
		.flatMap(i -> Observable.just(i)
		    .subscribeOn(scheduler)
		    .map(i2 -> intenseCalculation(i2))
		).finallyDo(() -> {
			time2 = System.currentTimeMillis();
			executor.shutdown();
			dur1 = (time2 - time1);
		})
		.subscribe(System.out::println);




		time3 = System.currentTimeMillis();
		Observable.range(0,999)
		.flatMap(i -> Observable.just(i)
		    .subscribeOn(scheduler2)
		    .map(i2 -> intenseCalculation(i2))
		).finallyDo(() -> {
			time4 = System.currentTimeMillis();
			executor2.shutdown();
			dur2 = (time4 - time3);
			printResult();
		})
		.subscribe(System.out::println);

	}



	private static int intenseCalculation(int i2) {
		return 1 + i2;
	}

	private static void printResult() {
		System.out.println("Duration 1: " + dur1);
		System.out.println("Duration 2: " + dur2);
	}

}


