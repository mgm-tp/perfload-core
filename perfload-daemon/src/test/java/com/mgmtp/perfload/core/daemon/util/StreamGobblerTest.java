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
package com.mgmtp.perfload.core.daemon.util;

import static com.google.common.base.Joiner.on;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.common.util.GobbleCallback;
import com.mgmtp.perfload.core.common.util.StreamGobbler;

/**
 * @author rnaegele
 */
public class StreamGobblerTest {

	@Test
	public void testGobbling() throws InterruptedException, ExecutionException, UnsupportedEncodingException {
		final Deque<String> linesStack = new ArrayDeque<String>(asList("foo ²³ bar 1", "foo µ bar 2", "foo € bar 3"));

		InputStream is = new ByteArrayInputStream(on('\n').join(linesStack).getBytes("UTF-8"));
		StreamGobbler gobbler = new StreamGobbler(new SynchronousExecService());
		gobbler.addStream(is, "UTF-8", new GobbleCallback() {
			@Override
			public void execute(final String line) {
				assertEquals(line, linesStack.pop());
			}
		}).get();
	}

	static class SynchronousExecService extends AbstractExecutorService {

		@Override
		public void shutdown() {
			//
		}

		@Override
		public List<Runnable> shutdownNow() {
			return null;
		}

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
			return false;
		}

		@Override
		public void execute(final Runnable command) {
			command.run();
		}
	}
}
