/*
 * Copyright (c) 2002-2014 mgm technology partners GmbH
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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An {@link ExecutorService} for delayed execution of tasks. Unlike a
 * {@link ScheduledThreadPoolExecutor} which is bounded by the core pool size creating all threads
 * at scheduling time, this class maintains an actively managed queue of tasks that are submitted to
 * a worker executor service when they are due. New threads are created when necessary. Idle threads
 * may be removed.
 * </p>
 * <p>
 * This class maintains an additional queue that completed tasks are added to. A callback may be
 * executed whenever a task is done (see {@link #setDoneCallback(Runnable)}).
 * </p>
 * 
 * @author rnaegele
 */
@ThreadSafe
public final class DelayingExecutorService extends AbstractExecutorService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final BlockingQueue<RunnableScheduledFuture<?>> workQueue = new DelayQueue<>();
	private final BlockingQueue<Future<?>> completionQueue = new LinkedBlockingQueue<>();

	private volatile Runnable doneCallback;

	private final ExecutorService bossExecutor;
	private final ThreadPoolExecutor workerExecutor;

	/**
	 * Sequence number to break scheduling ties, and in turn to guarantee FIFO order among tied
	 * entries.
	 */
	private final AtomicLong sequencer = new AtomicLong();

	/** Base of nanosecond timings, to avoid wrapping */
	private final long nanoOrigin = System.nanoTime();

	/**
	 * Creates a new instance.
	 */
	public DelayingExecutorService() {
		this.bossExecutor = Executors.newSingleThreadExecutor(new BossThreadFactory());
		this.workerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new WorkerThreadFactory());

		this.bossExecutor.submit(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						Runnable task = workQueue.take();
						log.info("Executing next due task...");
						workerExecutor.execute(task);
					} catch (InterruptedException ex) {
						log.info("Thread was interrupted.");
						return;
					}
				}
			}
		});
	}

	@Override
	public void shutdown() {
		workerExecutor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		bossExecutor.shutdownNow();
		return workerExecutor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return workerExecutor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return workerExecutor.isTerminated();
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
		return workerExecutor.awaitTermination(timeout, unit);
	}

	/**
	 * Retrieves the next available future representing a finished test thread and removes it from
	 * the completion queue. The method blocks until an element becomes avilable in the queue.
	 * 
	 * @return a future representing a finshed test thread
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public Future<?> takeNextCompleted() throws InterruptedException {
		return completionQueue.take();
	}

	/**
	 * Returns the approximate number of worker threads that are actively executing tasks.
	 * 
	 * @return the number of threads
	 */
	public int getActiveCount() {
		return workerExecutor.getActiveCount();
	}

	/**
	 * Returns the current number of threads in the pool.
	 * 
	 * @return the number of threads
	 */
	public int getPoolSize() {
		return workerExecutor.getPoolSize();
	}

	/**
	 * Returns the largest number of threads that have ever simultaneously been in the pool.
	 * 
	 * @return the number of threads
	 */
	public int getLargestPoolSize() {
		return workerExecutor.getLargestPoolSize();
	}

	/**
	 * Registers a callback that is executed whenever a task is completed.
	 * 
	 * @param doneCallback
	 *            the Runnable callback
	 */
	public void setDoneCallback(final Runnable doneCallback) {
		this.doneCallback = doneCallback;
	}

	/**
	 * Schedules the given command for immediate execution.
	 */
	@Override
	public void execute(final Runnable command) {
		schedule(command, 0, TimeUnit.NANOSECONDS);
	}

	/**
	 * Schedules a task to be executed at some time in the future. The time is express as a delay
	 * relative to the scheduling time.
	 * 
	 * @param <V>
	 *            the type of the task's result
	 * @param callable
	 *            the task
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the time unit for the delay
	 * @return the resulting future
	 */
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
		checkArgument(delay >= 0, "Delay must be greather than or equal to zero.");
		long triggerTime = now() + unit.toNanos(delay);
		RunnableScheduledFuture<V> t = new ScheduledFutureTask<>(callable, triggerTime, sequencer.getAndIncrement());
		executeDelayed(t);
		return t;
	}

	/**
	 * Schedules a task to be executed at some time in the future. The time is express as a delay
	 * relative to the scheduling time.
	 * 
	 * @param runnable
	 *            the task
	 * @param delay
	 *            the delay
	 * @param unit
	 *            the time unit for the delay
	 * @return the resulting future
	 */
	public ScheduledFuture<?> schedule(final Runnable runnable, final long delay, final TimeUnit unit) {
		checkArgument(delay >= 0, "Delay must be greather than or equal to zero.");
		long triggerTime = now() + unit.toNanos(delay);
		RunnableScheduledFuture<?> t = new ScheduledFutureTask<Void>(runnable, null, triggerTime, sequencer.getAndIncrement());
		executeDelayed(t);
		return t;
	}

	/**
	 * Returns nanosecond time offset by origin
	 */
	private long now() {
		return System.nanoTime() - nanoOrigin;
	}

	private void executeDelayed(final RunnableScheduledFuture<?> t) {
		if (workerExecutor.isShutdown()) {
			throw new RejectedExecutionException("Cannot schedule new tasks when executors service is already shut down.");
		}
		workQueue.add(t);
	}

	private static final class WorkerThreadFactory implements ThreadFactory {
		private final AtomicInteger threadCounter = new AtomicInteger(1);

		@Override
		public Thread newThread(final Runnable r) {
			final Thread th = new Thread(r, "LT_" + threadCounter.getAndIncrement());
			th.setDaemon(true);
			return th;
		}
	}

	private static final class BossThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(final Runnable r) {
			final Thread th = new Thread(r, "Boss");
			th.setDaemon(true);
			th.setPriority(Thread.NORM_PRIORITY + 1);
			return th;
		}
	}

	final class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

		/** Sequence number to break ties FIFO */
		private final long sequenceNumber;

		/** The time the task is enabled to execute in nanoTime units */
		private final long time;

		/**
		 * Creates a task with the given nanoTime-based trigger time.
		 */
		ScheduledFutureTask(final Runnable r, final V result, final long time, final long sequenceNumber) {
			this(Executors.callable(r, result), time, sequenceNumber);
		}

		/**
		 * Creates a task with the given nanoTime-based trigger.
		 */
		ScheduledFutureTask(final Callable<V> callable, final long time, final long sequenceNumber) {
			super(callable);
			this.time = time;
			this.sequenceNumber = sequenceNumber;
		}

		@Override
		protected void done() {
			completionQueue.add(this);
			if (doneCallback != null) {
				doneCallback.run();
			}
		}

		@Override
		public long getDelay(final TimeUnit unit) {
			return unit.convert(time - now(), TimeUnit.NANOSECONDS);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (sequenceNumber ^ sequenceNumber >>> 32);
			result = prime * result + (int) (time ^ time >>> 32);
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ScheduledFutureTask<?> other = (ScheduledFutureTask<?>) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (sequenceNumber != other.sequenceNumber) {
				return false;
			}
			if (time != other.time) {
				return false;
			}
			return true;
		}

		@Override
		public int compareTo(final Delayed other) {
			if (other == this) {
				return 0;
			}
			if (other instanceof ScheduledFutureTask) {
				ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
				long diff = time - x.time;
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				} else if (sequenceNumber < x.sequenceNumber) {
					return -1;
				} else {
					return 1;
				}
			}

			long d = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
			return d == 0 ? 0 : d < 0 ? -1 : 1;
		}

		private DelayingExecutorService getOuterType() {
			return DelayingExecutorService.this;
		}
	}
}
