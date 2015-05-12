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
package com.mgmtp.perfload.core.client.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

import net.jcip.annotations.NotThreadSafe;

import com.mgmtp.perfload.core.client.config.scope.ExecutionScoped;

/**
 * Pojo holding execution-related properties. The current instance must be fetched and initialized
 * in the same thread that executes the current test run before the actual test is executed. This
 * makes it possible to dynamically feed operation and target into a Guice-managed class.
 *
 * @author rnaegele
 */
@ExecutionScoped
@NotThreadSafe
public final class LtContext {

	private final UUID executionId = UUID.randomUUID();
	private String operation;
	private String target;
	private int threadId;

	/**
	 * @return the executionId
	 */
	public UUID getExecutionId() {
		return executionId;
	}

	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @param operation
	 *            the operation to set
	 */
	public void setOperation(final String operation) {
		checkState(this.operation == null, "'operation' already set");
		this.operation = operation;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(final String target) {
		checkState(this.target == null, "'target' already set");
		this.target = target;
	}

	/**
	 * @return the threadId
	 */
	public int getThreadId() {
		return threadId;
	}

	/**
	 * @param threadId
	 *            the threadId to set
	 */
	public void setThreadId(final int threadId) {
		checkState(this.threadId == 0, "'threadId' already set");
		this.threadId = threadId;
	}
}
