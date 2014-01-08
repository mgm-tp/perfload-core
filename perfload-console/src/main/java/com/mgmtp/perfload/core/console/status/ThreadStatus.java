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
package com.mgmtp.perfload.core.console.status;

/**
 * Simple POJO representing a test thread's status.
 * 
 * @author rnaegele
 */
public class ThreadStatus {

	private final Integer daemonId;
	private final Integer processId;
	private final Integer threadId;
	private final String operation;
	private final String target;
	private final boolean finished;
	private final String stackTrace;
	private final String result;

	public ThreadStatus(final Integer daemonId, final Integer processId, final Integer threadId, final String operation, final String target,
			final boolean finished, final String stackTrace, final String result) {
		this.daemonId = daemonId;
		this.processId = processId;
		this.threadId = threadId;
		this.operation = operation;
		this.target = target;
		this.finished = finished;
		this.stackTrace = stackTrace;
		this.result = result;
	}

	public Integer getDaemonId() {
		return daemonId;
	}

	public Integer getProcessId() {
		return processId;
	}

	public Integer getThreadId() {
		return threadId;
	}

	public String getOperation() {
		return operation;
	}

	public String getTarget() {
		return target;
	}

	public boolean isFinished() {
		return finished;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public String getResult() {
		return result;
	}
}
