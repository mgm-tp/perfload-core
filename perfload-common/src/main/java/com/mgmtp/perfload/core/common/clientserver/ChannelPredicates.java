/*
 * Copyright (c) 2002-2015 mgm technology partners GmbH
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
package com.mgmtp.perfload.core.common.clientserver;

import com.google.common.base.Predicate;

/**
 * Utility class containing predicates for filtering channels connected to a daemon.
 * 
 * @author rnaegele
 */
public final class ChannelPredicates {
	static final String CONSOLE = "console";
	static final String TESTPROC = "testproc";

	private ChannelPredicates() {
		// don't allow instantiation
	}

	/**
	 * Creates a new {@link Predicate} for console channels.
	 * 
	 * @return a {@link Predicate} which applies to channels whose id starts with "console".
	 */
	public static Predicate<String> isConsoleChannel() {
		return startsWith(CONSOLE);
	}

	/**
	 * Creates a new Predicate for test process channels.
	 * 
	 * @return a {@link Predicate} which applies to channels whose id starts with "testproc".
	 */
	public static Predicate<String> isTestprocChannel() {
		return startsWith(TESTPROC);
	}

	private static Predicate<String> startsWith(final String prefix) {
		return new StartsWithPredicate(prefix);
	}

	private static class StartsWithPredicate implements Predicate<String> {
		private final String prefix;

		public StartsWithPredicate(final String prefix) {
			this.prefix = prefix;
		}

		@Override
		public boolean apply(final String input) {
			return input.startsWith(prefix);
		}
	}
}
