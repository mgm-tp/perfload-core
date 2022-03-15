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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * <p>
 * Guice scope for executions. Uses thread-local storage to bind executionIds. A global cache by
 * executionId is used for the scope cache. Thus, threads may join an existing scope, which is e. g.
 * necessary for asynchronous HTTP operations.
 * </p>
 * <p>
 * The scope must initially be entered using {@link #enterScope(UUID, Map)}. Further threads may
 * join the scope calling {@link #joinScope(UUID)}. In order to free up resources and to avoid
 * memory leaks, {@link #disjoinScope(UUID)} and {@link #exitScope(UUID)} should be called in turn.
 * </p>
 * <p>
 * The scope should be bound as follows:
 *
 * <pre>
 * ExecutionScope executionScope = new ExecutionScope();
 * bindScope(ExecutionScoped.class, executionScope);
 * bind(ExecutionScope.class).toInstance(executionScope);
 * </pre>
 *
 * Class to be bound in this scope can be annotated with {@link ExecutionScoped}.
 * </p>
 *
 * @author rnaegele
 */
public final class ExecutionScope implements Scope {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionScope.class);

	private static final String MSG_NOT_ENTERED = "Scope has not been entered. Forgot to call enterScope()?";
	private static final String MSG_ALREADY_ENTERED = "Scope has already been entered. Forgot to call exitScope()?";

	private final ThreadLocal<UUID> threadLocalExecutionId = new ThreadLocal<>();

	private final Map<UUID, Map<Key<?>, Object>> scopeCaches = new HashMap<>();

	@Override
	public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
		return new ExecutionScopeProvider<>(key, unscoped);
	}

	@Override
	public String toString() {
		return "ExecutionScope";
	}

	private final class ExecutionScopeProvider<T> implements Provider<T> {
		private final Key<T> key;
		private final Provider<T> unscoped;

		private ExecutionScopeProvider(final Key<T> key, final Provider<T> unscoped) {
			this.key = key;
			this.unscoped = unscoped;
		}

		@Override
		public T get() {
			UUID executionId = threadLocalExecutionId.get();
			requireNonNull(executionId, MSG_NOT_ENTERED);

			//			UUID executionId = ref.get();
			//			requireNonNull(ref.get(), MSG_NOT_ENTERED);

			synchronized (this) {
				Map<Key<?>, Object> scopeCache = scopeCaches.computeIfAbsent(executionId, id -> new HashMap<>());

				@SuppressWarnings("unchecked")
				// cast ok, because we know what we'd put in before
				/* computeIfAbsent can no longer be used since Java 9 because it now throws a
				ConcurrentModificationException in this context
				T result = (T) scopeCache.computeIfAbsent(key, k -> unscoped.get());
				therefore, it has been replaced by this code: */
				T result;
				synchronized(this) {
					result = (T) scopeCache.get(key);
					if (result == null) {
						result = unscoped.get();
						scopeCache.put(key, result);
					}
				}
				return result;
			}
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", unscoped, ExecutionScope.this);
		}
	}

	/**
	 * Enters a new scope context for the current thread setting a scope map to the internal
	 * {@link ThreadLocal}.
	 *
	 * @throws IllegalStateException
	 *             if there is already a scope context for the given {@code executionId} or the
	 *             current thread
	 */
	public synchronized void enterScope(final UUID executionId, final Map<Key<?>, Object> scopeCache) {
		checkState(!scopeCaches.containsKey(executionId), MSG_ALREADY_ENTERED);
		checkState(threadLocalExecutionId.get() == null, MSG_ALREADY_ENTERED);
		threadLocalExecutionId.set(executionId);
		scopeCaches.put(executionId, scopeCache);
		LOGGER.debug("Entered scope for executionId: {}", executionId);
	}

	/**
	 * Join an existing scope for the given {@code executionId}.
	 *
	 * @param executionId
	 *            the executionId
	 * @throws IllegalStateException
	 *             if there is no scope context for the given {@code executionId}, i. e.
	 *             {@link #enterScope(UUID, Map)} has not been called
	 */
	public synchronized void joinScope(final UUID executionId) {
		checkState(scopeCaches.containsKey(executionId), MSG_NOT_ENTERED);
		threadLocalExecutionId.set(executionId);
		LOGGER.debug("Joined scope for executionId: {}", executionId);
	}

	/**
	 * Disjoin an existing scope for the given {@code executionId}.
	 *
	 * @param executionId
	 *            the executionId
	 * @throws IllegalStateException
	 *             if there is no scope context for the given {@code executionId}, i. e.
	 *             {@link #enterScope(UUID, Map)} has not been called
	 */
	public synchronized void disjoinScope(final UUID executionId) {
		checkState(scopeCaches.containsKey(executionId), MSG_NOT_ENTERED);
		threadLocalExecutionId.remove();
		LOGGER.debug("Disjoined scope for executionId: {}", executionId);
	}

	/**
	 * Exits the scope context for the current thread. Call this method after a thread is done in
	 * order to avoid memory leaks and to enable the thread to enter a new scope context again.
	 *
	 * @throws IllegalStateException
	 *             if there is no scope context for the given {@code executionId}, i. e.
	 *             {@link #enterScope(UUID, Map)} has not been called
	 */
	public synchronized void exitScope(final UUID executionId) {
		checkState(scopeCaches.containsKey(executionId), MSG_NOT_ENTERED);
		requireNonNull(threadLocalExecutionId.get() == null, MSG_NOT_ENTERED);
		threadLocalExecutionId.remove();
		scopeCaches.remove(executionId);
		LOGGER.debug("Exited scope for executionId: {}", executionId);
	}
}
