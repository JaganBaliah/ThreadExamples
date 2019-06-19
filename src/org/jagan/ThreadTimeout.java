package org.jagan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ThreadTimeout {

	public static void main(String[] args) {
		System.out.println("Hi");
		//testRunnable();
		//testCallable();	
		testControlledCallable();
	}

	private static void testControlledCallable() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		List<CTask> cTasks = new ArrayList<CTask>();
		IntStream.rangeClosed(1, 2).forEach((int a) -> {
			CTask cTask = new CTask(a,5);
			cTasks.add(cTask);
			Future<Integer> future = service.submit(cTask);
			futures.add(future);
		});		
		serviceShutDown(service, 4);		
		
		IntStream.rangeClosed(0, 1).forEach((int a) -> {
			//cTasks.get(a).keepRunning = false;
			try {
				cTasks.get(a).keepRunning = false;
				System.out.println("Future Get : " + a + " - " + futures.get(a).get());				
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		});
		
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Main Thread Exiting now...");
	}
	
	private static class CTask implements Callable<Integer> {

		private int i;
		private int sleepCycle;
		
		public volatile boolean keepRunning = true;
		
		public CTask(int i, int sleepCycle) {
			this.i = i;
			this.sleepCycle = sleepCycle;
		}
		
		@Override
		public Integer call() {
			Integer j = -1;
			System.out.println("Running : " + this.i);
			
				for(int k = sleepCycle; k >= 0; k--) {
					if(keepRunning) {
						try {
							Thread.sleep(1 * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(!keepRunning) {
						break;
					}					
				}
				if(keepRunning) j = new Random().nextInt(5);
			
			return j;
		}
	}

	@SuppressWarnings("unused")
	private static void testCallable() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		IntStream.rangeClosed(1, 2).forEach((int a) -> {
			Future<Integer> future = service.submit(new Task(a, 10));
			futures.add(future);
		});		
		serviceShutDown(service, 2);
		
		IntStream.rangeClosed(1, 2).forEach((int a) -> {
			/*
			try {
				System.out.println("Future Get : " + a + " - " + futures.get(a - 1).get());				
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			System.out.println("Future : " + a + " - " + futures.get(a - 1).cancel(true));
			
		});
				
	}
	
	private static class Task implements Callable<Integer> {

		private int i;
		private long sleepTime;
		
		public Task(int i, long sleepTime) {
			this.i = i;
			this.sleepTime = sleepTime;
		}
		
		@Override
		public Integer call() {
			System.out.println("Running : " + this.i);
			
			try {
				Thread.sleep(sleepTime * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Integer j = new Random().nextInt(5);
			return j;
		}
	}

	@SuppressWarnings("unused")
	private static void testRunnable() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		service.execute(new Job());
		service.execute(new Job());
		serviceShutDown(service, 4);
	}

	private static class Job implements Runnable {

		@Override
		public void run() {
			System.out.println("Started Executing : " + Thread.currentThread().getName());
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done Executing : " + Thread.currentThread().getName());
		}
	}
	
	private static void serviceShutDown(ExecutorService service, long timeout) {
		service.shutdown();
		boolean awaitTermination = false; 
		try {
			awaitTermination = service.awaitTermination(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Shutdown Status : " + awaitTermination);
	}

}
