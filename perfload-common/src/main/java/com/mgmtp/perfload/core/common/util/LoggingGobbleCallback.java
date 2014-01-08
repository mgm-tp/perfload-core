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
package com.mgmtp.perfload.core.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GobbleCallback} implementation that logs lines.
 * 
 * @author rnaegele
 */
public class LoggingGobbleCallback implements GobbleCallback {
	private final String prefix;
	private final Level logLevel;

	/**
	 * @param prefix
	 *            some string that is prepended to the log message, if non-null.
	 */
	public LoggingGobbleCallback(final Level logLevel, final String prefix) {
		this.logLevel = logLevel;
		this.prefix = prefix;
	}

	/**
	 * Logs the line with the specified level, prepending the prefix, if applicable.
	 */
	@Override
	public void execute(final String line) {
		logLevel.log(prefix != null ? prefix + line : line);
	}

	public static enum Level {
		ERROR {
			@Override
			public void log(final String value) {
				log.error(value);
			}
		},
		WARN {
			@Override
			public void log(final String value) {
				log.warn(value);
			}
		},
		INFO {
			@Override
			public void log(final String value) {
				log.info(value);
			}
		},
		DEBUG {
			@Override
			public void log(final String value) {
				log.debug(value);
			}
		},
		TRACE {
			@Override
			public void log(final String value) {
				log.trace(value);
			}
		},
		OFF {
			@Override
			public void log(final String value) {
				//
			}
		};

		Logger log = LoggerFactory.getLogger(LoggingGobbleCallback.class);

		public abstract void log(String value);
	}
}
