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
package com.mgmtp.perfload.core.common.config;

import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.io.Serializable;

/**
 * Simple pojo representing a load profile event.
 * 
 * @author rnaegele
 */
public class LoadProfileEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private final long startTime;
	private final String operation;
	private final String target;
	private final int daemonId;
	private final int processId;

	/**
	 * @param startTime
	 *            The start time at which a new test thread is started
	 * @param operation
	 *            The operation name, i. e. some arbitrary value the test can deal with
	 * @param target
	 *            The target, i. e. some arbitrary value that test can deal with
	 * @param daemonId
	 *            The if of the daemon the test should be run on
	 * @param processId
	 *            The id of the process that should spawn the test thread
	 */
	public LoadProfileEvent(final long startTime, final String operation, final String target, final int daemonId,
	        final int processId) {
		this.startTime = startTime;
		this.operation = operation;
		this.target = target;
		this.daemonId = daemonId;
		this.processId = processId;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getOperation() {
		return operation;
	}

	public String getTarget() {
		return target;
	}

	public int getDaemonId() {
		return daemonId;
	}

	public int getProcessId() {
		return processId;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
