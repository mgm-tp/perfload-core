/*
 * Copyright (c) 2013 mgm technology partners GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgmtp.perfload.core.client.util.concurrent;

import static java.lang.Math.abs;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.util.concurrent.DelayingExecutorService;

/**
 * @author rnaegele
 */
public class DelayingExecutorServiceTest {

	private static final long EPSILON = 200L;

	@Test
	public void testWithoutDelay() throws InterruptedException, BrokenBarrierException {
		DelayingExecutorService execSrv = new DelayingExecutorService();

		final StopWatch sw = new StopWatch();

		final CyclicBarrier stopBarrier = new CyclicBarrier(11, new Runnable() {
			@Override
			public void run() {
				sw.stop();
			}
		});

		sw.start();

		for (int i = 0; i < 10; ++i) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1L);
						stopBarrier.await();
					} catch (Exception ex) {
						throw new AssertionError(ex);
					}
				}
			};

			ScheduledFuture<?> future = execSrv.schedule(r, 0L, TimeUnit.NANOSECONDS);

			// compare with epsilon to make up for bad accuracy
			assertTrue(abs(future.getDelay(TimeUnit.MILLISECONDS)) < EPSILON);
		}

		stopBarrier.await();
		Thread.sleep(1000L);

		assertTrue(sw.getTime() < EPSILON);

		sw.reset();
		sw.start();

		for (int i = 0; i < 10; ++i) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1L);
						stopBarrier.await();
					} catch (Exception ex) {
						throw new AssertionError(ex);
					}
				}
			};

			ScheduledFuture<?> future = execSrv.schedule(r, 0L, TimeUnit.NANOSECONDS);

			// compare with epsilon to make up for bad accuracy
			assertTrue(abs(future.getDelay(TimeUnit.MILLISECONDS)) < EPSILON);
		}

		execSrv.shutdown();

		assertTrue(sw.getTime() < EPSILON);
	}

	@Test
	public void testWithDelay() throws InterruptedException, BrokenBarrierException {
		DelayingExecutorService execSrv = new DelayingExecutorService();

		final StopWatch sw = new StopWatch();

		final CyclicBarrier stopBarrier = new CyclicBarrier(11, new Runnable() {
			@Override
			public void run() {
				sw.stop();
			}
		});

		sw.start();

		final long taskSleepMillis = 75L;
		long delayMultiplier = 50L;
		int loopMax = 10;

		for (int i = 0; i < loopMax; ++i) {
			Callable<Void> c = new Callable<Void>() {
				@Override
				public Void call() {
					try {
						Thread.sleep(taskSleepMillis);
						stopBarrier.await();
					} catch (Exception ex) {
						throw new AssertionError(ex);
					}
					return null;
				}
			};

			long delay = delayMultiplier * i;

			ScheduledFuture<?> future = execSrv.schedule(c, delay, TimeUnit.MILLISECONDS);
			long actualDelay = future.getDelay(TimeUnit.MILLISECONDS);

			// compare with epsilon to make up for bad accuracy
			assertTrue(abs(delay - actualDelay) < EPSILON);
		}

		stopBarrier.await();

		long actualTime = sw.getTime();
		long expectedTime = delayMultiplier * (loopMax - 1) + taskSleepMillis;

		// compare with epsilon to make up for bad accuracy
		assertTrue(abs(actualTime - expectedTime) < EPSILON);
	}

	@Test
	public void testWithCompletionQueue() throws InterruptedException, ExecutionException {
		DelayingExecutorService execSrv = new DelayingExecutorService();

		final long taskSleepMillis = 75L;
		long delayMultiplier = 50L;

		for (int i = 0; i < 10; ++i) {
			final int index = i;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(taskSleepMillis);
						System.out.printf("Thread #%d\n", index);
					} catch (Exception ex) {
						throw new AssertionError(ex);
					}
				}
			};

			long delay = delayMultiplier * i;
			execSrv.schedule(r, delay, TimeUnit.MILLISECONDS);
		}
		for (int i = 10; i < 20; ++i) {
			final int index = i;
			Callable<Void> c = new Callable<Void>() {
				@Override
				public Void call() {
					try {
						Thread.sleep(taskSleepMillis);
						System.out.printf("Thread #%d\n", index);
					} catch (Exception ex) {
						throw new AssertionError(ex);
					}
					return null;
				}
			};

			long delay = delayMultiplier * i;
			execSrv.schedule(c, delay, TimeUnit.MILLISECONDS);
		}

		for (int i = 0; i < 20; ++i) {
			System.out.printf("Take #%d\n", i);
			execSrv.takeNextCompleted().get();
		}
	}
}
