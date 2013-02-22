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
package com.mgmtp.perfload.core.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class LtUtilsTest {

	@Test
	public void testInterrupt() throws InterruptedException {
		ExecutorService execService = Executors.newSingleThreadExecutor();

		// The latch makes sure checkInterrupt is called at least twice, at least once without the thread being interrupted and
		// once with the thread being interrupted.
		final CountDownLatch latch = new CountDownLatch(1);

		Future<?> future = execService.submit(new Runnable() {
			@Override
			public void run() {
				while (true) {
					LtUtils.checkInterrupt();
					latch.countDown();
				}
			}
		});

		latch.await();
		execService.shutdownNow();

		try {
			future.get();
		} catch (ExecutionException ex) {
			assertThat(ex.getCause(), is(instanceOf(AbortionException.class)));
		}
	}
}