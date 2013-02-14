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
package com.mgmtp.perfload.core.common.clientserver;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Collections2;

/**
 * @author rnaegele
 */
public class ChannelPredicatesTest {

	@Test
	public void testChannelPredicates() {
		List<String> testStrings = newArrayList(
		        ChannelPredicates.CONSOLE,
		        ChannelPredicates.TESTPROC,
		        ChannelPredicates.TESTPROC,
		        ChannelPredicates.TESTPROC
		        );

		Collection<String> consoleStrings = Collections2.filter(testStrings, ChannelPredicates.isConsoleChannel());
		assertEquals(consoleStrings.size(), 1);
		assertThat(consoleStrings, hasItem(ChannelPredicates.CONSOLE));
		assertThat(consoleStrings, not(hasItem(ChannelPredicates.TESTPROC)));

		Collection<String> testprocStrings = Collections2.filter(testStrings, ChannelPredicates.isTestprocChannel());
		assertEquals(testprocStrings.size(), 3);
		assertThat(testprocStrings, hasItem(ChannelPredicates.TESTPROC));
		assertThat(testprocStrings, not(hasItem(ChannelPredicates.CONSOLE)));
	}
}
