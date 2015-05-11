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
package com.mgmtp.perfload.core.client.config;

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.mgmtp.perfload.core.client.config.scope.ThreadScope;

/**
 * @author rnaegele
 */
public class ThreadScopeTest {

	@Test
	public void testWithSingleThread() {
		Set<Object> objects = newHashSet();
		ThreadScope ts = new ThreadScope();
		Key<Object> key = Key.get(Object.class);
		Provider<Object> provider = new ObjectProvider();

		for (int i = 0; i < 5; ++i) {
			Object scoped = ts.scope(key, provider).get();
			objects.add(scoped);
		}

		assertEquals(objects.size(), 1);

		ts.cleanUp();

		for (int i = 0; i < 5; ++i) {
			Object scoped = ts.scope(key, provider).get();
			objects.add(scoped);
		}

		assertEquals(objects.size(), 2);
	}

	@Test
	public void testWithMultipleThreads() throws InterruptedException, ExecutionException {
		int threadCount = 5;

		final Set<Object> objects = newHashSet();
		final ThreadScope ts = new ThreadScope();
		final Key<Object> key = Key.get(Object.class);
		final Provider<Object> provider = new ObjectProvider();
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					Object scoped = ts.scope(key, provider).get();
					objects.add(scoped);
				}
			}).get();
		}

		assertEquals(objects.size(), threadCount);

		ts.cleanUp();

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					ts.cleanUp();

					Provider<Object> scopedProvider = ts.scope(key, provider);
					assertEquals(scopedProvider.toString(), "unscoped provider[ThreadScope]");

					Object scoped = scopedProvider.get();
					objects.add(scoped);
				}
			}).get();
		}

		assertEquals(objects.size(), 2 * threadCount);
	}

	private static class ObjectProvider implements Provider<Object> {
		@Override
		public Object get() {
			return new Object();
		}

		@Override
		public String toString() {
			return "unscoped provider";
		}
	}
}
