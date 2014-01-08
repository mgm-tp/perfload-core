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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Simple POJO representing the thread activity status of a test process.
 * 
 * @author rnaegele
 */
public class ThreadActivity {
	private final Integer daemonId;
	private final Integer processId;
	private final int activeThreads;
	private final String timestamp;

	/**
	 * @param daemonId
	 *            the daemon id
	 * @param processId
	 *            the process id
	 * @param activeThreads
	 *            the number of active threads
	 * @param timestamp
	 *            an ISO8601 timestamp including milliseconds and timezone
	 */
	public ThreadActivity(final Integer daemonId, final Integer processId, final int activeThreads, final String timestamp) {
		this.daemonId = daemonId;
		this.processId = processId;
		this.activeThreads = activeThreads;
		this.timestamp = timestamp;
	}

	public int getDaemonId() {
		return daemonId;
	}

	public int getProcessId() {
		return processId;
	}

	public int getActiveThreads() {
		return activeThreads;
	}

	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
