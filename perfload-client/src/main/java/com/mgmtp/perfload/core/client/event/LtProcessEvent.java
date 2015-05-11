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
package com.mgmtp.perfload.core.client.event;

import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import com.mgmtp.perfload.core.client.LtProcess;
import com.mgmtp.perfload.core.common.util.LtStatus;

/**
 * Represents an event triggered by {@link LtProcess} on process start and finish.
 * 
 * @author rnaegele
 * @see LtProcessEventListener
 */
public final class LtProcessEvent {

	private final int processId;
	private final int daemonId;
	private final LtStatus result;

	/**
	 * @param processId
	 *            the one-based id of the process
	 * @param daemonId
	 *            the one-based id of the daemon
	 * @param result
	 *            the result of the test
	 */
	public LtProcessEvent(final int processId, final int daemonId, final LtStatus result) {
		this.processId = processId;
		this.daemonId = daemonId;
		this.result = result;
	}

	/**
	 * @param processId
	 *            the one-based id of the process
	 * @param daemonId
	 *            the one-based id of the daemon
	 */
	public LtProcessEvent(final int processId, final int daemonId) {
		this(processId, daemonId, null);
	}

	/**
	 * @return the processId
	 */
	public int getProcessId() {
		return processId;
	}

	/**
	 * @return the daemonId
	 */
	public int getDaemonId() {
		return daemonId;
	}

	/**
	 * @return the result
	 */
	public LtStatus getResult() {
		return result;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
