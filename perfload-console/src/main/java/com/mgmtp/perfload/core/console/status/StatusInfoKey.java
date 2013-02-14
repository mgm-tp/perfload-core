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
package com.mgmtp.perfload.core.console.status;

/**
 * Represents a key for holding status information.
 * 
 * @author rnaegele
 */
public class StatusInfoKey implements Comparable<StatusInfoKey> {

	private final Integer daemonId;
	private final Integer processId;
	private final Integer threadId;

	StatusInfoKey(final Integer daemonId, final Integer processId, final Integer threadId) {
		this.daemonId = daemonId;
		this.processId = processId;
		this.threadId = threadId;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (daemonId == null ? 0 : daemonId.hashCode());
		result = prime * result + (processId == null ? 0 : processId.hashCode());
		result = prime * result + (threadId == null ? 0 : threadId.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StatusInfoKey other = (StatusInfoKey) obj;
		if (daemonId == null) {
			if (other.daemonId != null) {
				return false;
			}
		} else if (!daemonId.equals(other.daemonId)) {
			return false;
		}
		if (processId == null) {
			if (other.processId != null) {
				return false;
			}
		} else if (!processId.equals(other.processId)) {
			return false;
		}
		if (threadId == null) {
			if (other.threadId != null) {
				return false;
			}
		} else if (!threadId.equals(other.threadId)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(final StatusInfoKey other) {
		int result = daemonId.compareTo(other.daemonId);
		if (result == 0) {
			result = processId.compareTo(other.processId);
		}
		if (result == 0 && threadId != null && other.threadId != null) {
			result = threadId.compareTo(other.threadId);
		}
		return result;
	}
}
