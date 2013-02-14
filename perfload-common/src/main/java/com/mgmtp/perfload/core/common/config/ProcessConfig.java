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

import static com.google.common.base.Preconditions.checkArgument;
import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Comprises information necessary to start a test process.
 * 
 * @author rnaegele
 */
public class ProcessConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int processId;
	private final int daemonId;
	private final List<String> jvmArgs;

	/**
	 * @param processId
	 *            the 1-based integer id of the process
	 * @param daemonId
	 *            the 1-based integer id of the daemon that starts the process
	 * @param jvmArgs
	 *            a list of VM arguments for the Java process
	 */
	public ProcessConfig(final int processId, final int daemonId, final List<String> jvmArgs) {
		checkArgument(processId > 0, "'processId' must be an integer value greater than 0.");
		checkArgument(daemonId > 0, "'daemonId' must be an integer value greater than 0.");
		checkArgument(jvmArgs != null, "'jvmArgs' must not be null.");

		this.processId = processId;
		this.daemonId = daemonId;
		this.jvmArgs = ImmutableList.copyOf(jvmArgs);
	}

	/**
	 * @param processId
	 *            the 1-based integer id of the process
	 * @param daemonId
	 *            the 1-based integer id of the daemon that starts the process
	 */
	public ProcessConfig(final int processId, final int daemonId) {
		this(processId, daemonId, Collections.<String>emptyList());
	}

	public int getProcessId() {
		return processId;
	}

	public int getDaemonId() {
		return daemonId;
	}

	/**
	 * @return an immutable list of VM properties
	 */
	public List<String> getJvmArgs() {
		return jvmArgs;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
