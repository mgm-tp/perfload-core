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

import static org.testng.Assert.assertEquals;

import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;

import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback;
import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback.Level;

/**
 * @author rnaegele
 */
public class LoggingGobbleCallbackTest {

	@Test
	public void testLevels() {
		for (Level level : Level.values()) {
			doTest(level, "foo", null);
			doTest(level, "foo", "prefix");
		}
	}

	private void doTest(final Level level, final String msg, final String prefix) {
		Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(final ILoggingEvent eventObject) {
				assertEquals(eventObject.getFormattedMessage(), prefix != null ? prefix + msg : msg);
				assertEquals(eventObject.getLevel().toString(), level.name());
			}
		};

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.reset();
		appender.setContext(lc);
		appender.start();
		lc.getLogger(LoggingGobbleCallback.class).addAppender(appender);

		LoggingGobbleCallback callback = new LoggingGobbleCallback(level, prefix);
		callback.execute("foo");
	}
}
