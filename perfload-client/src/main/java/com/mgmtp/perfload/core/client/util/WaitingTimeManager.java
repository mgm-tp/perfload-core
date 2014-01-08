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
package com.mgmtp.perfload.core.client.util;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;

/**
 * Uitility class for managing waiting times.
 * 
 * @author rnaegele
 */
@ThreadScoped
public final class WaitingTimeManager {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final WaitingTimeStrategy beforeRequestStrategy;
	private final long beforeTestStartMillis;
	private final Random rnd = new Random();

	/**
	 * @param beforeTestStartMillis
	 *            sleep time in milliseconds before the test starts
	 * @param beforeRequestStrategy
	 *            strategy used to calculate the sleep time before each request
	 */
	@Inject
	public WaitingTimeManager(@Named("wtm.beforeTestStartMillis") final long beforeTestStartMillis,
			final WaitingTimeStrategy beforeRequestStrategy) {
		this.beforeTestStartMillis = beforeTestStartMillis;
		this.beforeRequestStrategy = beforeRequestStrategy;
	}

	/**
	 * Sleeps the time configured with {@code beforeTestStartMillis}.
	 */
	public void sleepBeforeTestStart() {
		if (beforeTestStartMillis > 0L) {
			sleep("sleepBeforeTestStart", rnd.nextInt((int) beforeTestStartMillis));
		}
	}

	/**
	 * Sleeps the time determined by {@code beforeRequestStrategy}.
	 */
	public void sleepBeforeRequest() {
		long delay = beforeRequestStrategy.calculateWaitingTime();
		sleep("sleepBeforeRequest", delay);
	}

	/**
	 * Sleeps the given amount of time. This method catches the {@link InterruptedException} thrown
	 * by {@link Thread#sleep(long)} and restores the interrupt status interrupting the current
	 * thread again.
	 * 
	 * @param millis
	 *            the sleep time in milliseconds.
	 */
	private void sleep(final String method, final long millis) {
		if (millis > 0L) {
			try {
				log.info("{}: {} ms", method, millis);
				Thread.sleep(millis);
			} catch (InterruptedException ex) {
				// Restore interrupted status, so it can be handled later,
				// e. g. when a test is aborted
				Thread.currentThread().interrupt();
			}
		}
	}
}
