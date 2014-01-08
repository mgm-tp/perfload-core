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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.client.runner.LtRunnerTest.MockDriver.Action;
import com.mgmtp.perfload.core.client.util.ConstantWaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;

/**
 * @author rnaegele
 */
public class LtRunnerTest {

	@AfterMethod
	public void resetInterruptStatus() {
		// reset interrupt status because this might affect other tests
		Thread.interrupted();
	}

	@Test
	public void testNormalExecution() {
		MockListener listener = new MockListener();
		MockDriver driver = new MockDriver(Action.success);

		LtRunner runner = new LtRunner(driver, new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)),
				ImmutableSet.<LtRunnerEventListener>of(listener), new DefaultErrorHandler());
		runner.execute();

		assertEquals(driver.calls, 1);
		assertEquals(listener.calls, 2);
	}

	@Test
	public void testInterrupt() {
		MockListener listener = new MockListener();

		LtRunner runner = new LtRunner(new MockDriver(Action.success), new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)),
				ImmutableSet.<LtRunnerEventListener>of(listener), new DefaultErrorHandler());

		Thread.currentThread().interrupt();
		runAndAssertAbortionException(runner, LtStatus.INTERRUPTED);
	}

	@Test
	public void testErrors() {
		LtRunner runner = new LtRunner(new MockDriver(Action.abort), new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)),
				Collections.<LtRunnerEventListener>emptySet(), new DefaultErrorHandler());

		runAndAssertAbortionException(runner, LtStatus.ERROR);

		runner = new LtRunner(new MockDriver(Action.interrupt), new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)),
				Collections.<LtRunnerEventListener>emptySet(), new DefaultErrorHandler());

		runAndAssertAbortionException(runner, LtStatus.INTERRUPTED);

		Thread.interrupted(); // Clear potential interrupt status
		runner = new LtRunner(new MockDriver(Action.exception), new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)),
				Collections.<LtRunnerEventListener>emptySet(), new DefaultErrorHandler());
		runner.execute();
	}

	private void runAndAssertAbortionException(final LtRunner runner, final LtStatus status) {
		try {
			runner.execute();
			fail("Expected AbortionException.");
		} catch (AbortionException ex) {
			assertEquals(ex.getStatus(), status);
		}
	}

	static class MockDriver implements LtDriver {
		enum Action {
			success, abort, interrupt, exception
		}

		final Action action;

		MockDriver(final Action action) {
			this.action = action;
		}

		int calls;

		@Override
		public void execute() throws Exception {
			switch (action) {
				case abort:
					throw new AbortionException(LtStatus.ERROR);
				case interrupt:
					throw new InterruptedException();
				case exception:
					throw new Exception("some test exception");
				default:
					//
			}
			calls++;
		}
	}

	static class MockListener implements LtRunnerEventListener {
		int calls;

		@Override
		public void runStarted(final LtRunnerEvent event) {
			calls++;
		}

		@Override
		public void runFinished(final LtRunnerEvent event) {
			calls++;
		}
	}
}
