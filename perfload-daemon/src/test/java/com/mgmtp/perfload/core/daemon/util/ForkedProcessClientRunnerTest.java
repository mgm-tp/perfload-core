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
package com.mgmtp.perfload.core.daemon.util;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.common.config.ProcessConfig;
import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback;
import com.mgmtp.perfload.core.common.util.StreamGobbler;

/**
 * Unit test for {@link ForkedProcessClientRunner}.
 * 
 * @author rnaegele
 */
public class ForkedProcessClientRunnerTest {

	@Test
	public void testProcessExecution() throws InterruptedException, ExecutionException {
		final StringBuilder sb = new StringBuilder();

		Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(final ILoggingEvent eventObject) {
				sb.append(eventObject.toString());
			}
		};

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		appender.setContext(lc);
		appender.setName("test");
		appender.start();
		lc.getLogger(LoggingGobbleCallback.class).addAppender(appender);

		ExecutorService executor = Executors.newCachedThreadPool();
		StreamGobbler gobbler = new StreamGobbler(executor);
		ForkedProcessClientRunner fpcr = new ForkedProcessClientRunner(executor, gobbler);
		ProcessConfig proConf = new ProcessConfig(1, 1, ImmutableList.<String>of());

		// Start the process and wait for it
		Future<Integer> future = fpcr.runClient(new File("."), proConf, ImmutableList.<String>of());
		future.get();

		// Check for a class NoClassDefFoundError because the class LtProcess cannot be found. We just want to make sure
		// that we get output from the process. This guarantees that the process was running. The exception may be localized
		// if a JRE is used, so we can just check for error and the class name.
		assertTrue(sb.indexOf("ERROR") >= 0 && sb.indexOf("LtProcess") >= 0);
	}
}
