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
package com.mgmtp.perfload.core.client.runner;

import static com.mgmtp.perfload.core.common.util.LtUtils.checkInterrupt;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.common.util.LtUtils;

/**
 * This class is responsible for executing {@link LtDriver} implementations.
 *
 * @author rnaegele
 */
public final class LtRunner {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Set<LtRunnerEventListener> listeners;
	private final LtDriver driver;
	private final WaitingTimeManager waitingTimeManager;
	private final ErrorHandler errorHandler;

	/**
	 * @param driver
	 *            the load test driver to execute
	 * @param waitingTimeManager
	 *            determines waiting times between requests
	 * @param listeners
	 *            a set of event listeners
	 * @param errorHandler
	 *            the error handler that decides what to do if exceptions occur
	 */
	@Inject
	public LtRunner(final LtDriver driver, final WaitingTimeManager waitingTimeManager,
			final Set<LtRunnerEventListener> listeners, final ErrorHandler errorHandler) {
		this.driver = driver;
		this.waitingTimeManager = waitingTimeManager;
		this.listeners = listeners;
		this.errorHandler = errorHandler;
	}

	/**
	 * Executes the {@link LtDriver load test driver} implementation triggering
	 * {@link LtRunnerEvent}s. The interrupt status is checked before executing the driver
	 * calling {@link LtUtils#checkInterrupt()}. An interrupted thread leads to the abortion of
	 * the whole test.
	 *
	 * @see LtRunnerEventListener
	 */
	public void execute() {
		Throwable throwable = null;
		try {
			waitingTimeManager.sleepBeforeTestStart();
			fireRunStarted();

			checkInterrupt();
			driver.execute();
		} catch (InterruptedException ex) {
			throwable = ex;

			// If an InterruptedException is thrown, a thread's interrupt status is reset.
			// Thus, we need to interrupt it again.
			Thread.currentThread().interrupt();

			// Check for the interrupt causing the abortion of the test.
			checkInterrupt();
		} catch (Throwable th) {
			throwable = th;
			errorHandler.execute(th);
		} finally {
			fireRunFinished(throwable);
		}
	}

	private void fireRunStarted() {
		LtRunnerEvent event = new LtRunnerEvent();
		log.debug("fireRunStarted: {}", event);
		for (LtRunnerEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.runStarted(event);
		}
	}

	private void fireRunFinished(final Throwable throwable) {
		LtRunnerEvent event = new LtRunnerEvent(throwable);
		log.debug("fireRunFinished: {}", event);
		for (LtRunnerEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.runFinished(event);
		}
	}
}
