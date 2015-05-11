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
package com.mgmtp.perfload.core.test.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.event.LtListenerAdapter;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEvent;

/**
 * @author rnaegele
 */
@Singleton
@Immutable
@ThreadSafe
public class FibonacciListener extends LtListenerAdapter {
	private final Provider<PlaceholderContainer> placeholderContainerProvider;
	private final List<Entry<String, String>> testDataEntries;
	private final Random random = new Random();

	@Inject
	public FibonacciListener(final Provider<PlaceholderContainer> placeholderContainerProvider,
			@TestData final Map<String, String> testData) {
		this.placeholderContainerProvider = placeholderContainerProvider;
		this.testDataEntries = ImmutableList.copyOf(testData.entrySet());
	}

	@Override
	public void beforeRequest(final RequestFlowEvent event) {
		int index = random.nextInt(testDataEntries.size());
		Entry<String, String> entry = testDataEntries.get(index);

		PlaceholderContainer placeholderContainer = placeholderContainerProvider.get();
		placeholderContainer.put("n", entry.getKey());
		placeholderContainer.put("fibn", entry.getValue());
	}
}
