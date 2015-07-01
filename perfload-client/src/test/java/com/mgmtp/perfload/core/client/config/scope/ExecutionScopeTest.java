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
package com.mgmtp.perfload.core.client.config.scope;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.Test;

import com.google.common.base.Throwables;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @author rnaegele
 */
public class ExecutionScopeTest {

	@Test
	public void testEnterExitWithSingleThread() {
		Set<Object> objects = new HashSet<>();
		ExecutionScope es = new ExecutionScope();
		Key<Object> key = Key.get(Object.class);
		Provider<Object> provider = new ObjectProvider();

		UUID uuid = UUID.randomUUID();
		es.enterScope(uuid, new HashMap<>());

		for (int i = 0; i < 5; ++i) {
			Object scoped = es.scope(key, provider).get();
			objects.add(scoped);
		}

		assertThat(objects.size()).isEqualTo(1);

		es.exitScope(uuid);
		es.enterScope(uuid, new HashMap<>());

		for (int i = 0; i < 5; ++i) {
			Object scoped = es.scope(key, provider).get();
			objects.add(scoped);
		}

		es.exitScope(uuid);

		assertThat(objects.size()).isEqualTo(2);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testNotEntered() {
		ExecutionScope es = new ExecutionScope();
		Key<Object> key = Key.get(Object.class);
		Provider<Object> provider = new ObjectProvider();
		es.scope(key, provider).get();
		fail("Scope should not have been entered.");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testJoinNotEntered() {
		ExecutionScope es = new ExecutionScope();
		es.joinScope(UUID.randomUUID());
		fail("Scope should not have been entered.");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testDisjoinNotEntered() {
		ExecutionScope es = new ExecutionScope();
		es.disjoinScope(UUID.randomUUID());
		fail("Scope should not have been entered.");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testDisjoinNotJoined() {
		ExecutionScope es = new ExecutionScope();
		es.enterScope(UUID.randomUUID(), new HashMap<>());
		es.disjoinScope(UUID.randomUUID());
		fail("Scope should not have been entered.");
	}

	@Test
	public void testEnterExitWithMultipleThreads() throws InterruptedException, ExecutionException {
		int threadCount = 5;

		final Set<Object> objects = new HashSet<>();
		final ExecutionScope es = new ExecutionScope();
		final Key<Object> key = Key.get(Object.class);
		final Provider<Object> provider = new ObjectProvider();
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(() -> {
				UUID uuid = UUID.randomUUID();
				es.enterScope(uuid, new HashMap<>());
				Object scoped = es.scope(key, provider).get();
				objects.add(scoped);
				es.exitScope(uuid);
			}).get();
		}

		assertThat(objects.size()).isEqualTo(threadCount);

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(() -> {
				UUID uuid = UUID.randomUUID();
				es.enterScope(uuid, new HashMap<>());

				Provider<Object> scopedProvider = es.scope(key, provider);
				assertThat(scopedProvider.toString()).isEqualTo("unscoped provider[ExecutionScope]");

				Object scoped = scopedProvider.get();
				objects.add(scoped);
				es.exitScope(uuid);
			}).get();
		}

		assertThat(objects.size()).isEqualTo(2 * threadCount);
	}

	@Test
	public void testJoinDisjoinWithMultipleThreads() throws InterruptedException, ExecutionException {
		int threadCount = 5;

		final Set<Object> objects = new HashSet<>();
		final ExecutionScope es = new ExecutionScope();
		final Key<Object> key = Key.get(Object.class);
		final Provider<Object> provider = new ObjectProvider();
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);

		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		CountDownLatch latch1 = new CountDownLatch(2);
		CountDownLatch latch2 = new CountDownLatch(10);

		Future<?> future1 = exec.submit(() -> {
			try {
				es.enterScope(uuid1, new HashMap<>());
				Object scoped = es.scope(key, provider).get();
				objects.add(scoped);
				latch1.countDown();
				latch2.await();
				es.exitScope(uuid1);
			} catch (InterruptedException ex) {
				Throwables.propagate(ex);
			}
		});

		Future<?> future2 = exec.submit(() -> {
			try {
				es.enterScope(uuid2, new HashMap<>());
				Object scoped = es.scope(key, provider).get();
				objects.add(scoped);
				latch1.countDown();
				latch2.await();
				es.exitScope(uuid2);
			} catch (InterruptedException ex) {
				Throwables.propagate(ex);
			}
		});

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(() -> {
				es.joinScope(uuid1);
				Object scoped = es.scope(key, provider).get();
				objects.add(scoped);
				es.disjoinScope(uuid1);
				latch2.countDown();
			}).get();
		}

		for (int i = 0; i < threadCount; ++i) {
			exec.submit(() -> {
				es.joinScope(uuid2);
				Object scoped = es.scope(key, provider).get();
				objects.add(scoped);
				es.disjoinScope(uuid2);
				latch2.countDown();
			}).get();
		}

		future1.get();
		future2.get();

		// two objects because we had two different executionIds
		assertThat(objects.size()).isEqualTo(2);
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
