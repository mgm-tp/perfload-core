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

import static ch.lambdaj.Lambda.max;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sum;
import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Represents a test configuration. Since tests are ultimately started by the test daemons, a
 * {@link Config} object contains a list of {@link DaemonConfig} objects.
 * 
 * @author rnaegele
 */
public class Config implements Serializable {
	private static final long serialVersionUID = 1L;

	private final List<DaemonConfig> daemonConfigs = newArrayList();

	private String testplanFileName;

	/**
	 * Add a daemon configuration
	 * 
	 * @param daemonConfig
	 *            The {@link DaemonConfig} object
	 */
	public void addDaemonConfig(final DaemonConfig daemonConfig) {
		daemonConfigs.add(daemonConfig);
	}

	/**
	 * Returns an immutable copy of daemon configurations.
	 * 
	 * @return a list of {@link DaemonConfig} object
	 */
	public List<DaemonConfig> getDaemonConfigs() {
		return ImmutableList.copyOf(daemonConfigs);
	}

	/**
	 * Returns the total number of test process configured across all daemons.
	 * 
	 * @return The number of processes
	 */
	public int getTotalProcessCount() {
		int processes = 0;
		for (DaemonConfig dc : daemonConfigs) {
			processes += dc.getProcessConfigs().size();
		}
		return processes;
	}

	/**
	 * Returns the total number of jar files that have to be sent to the daemons.
	 * 
	 * @return The number of jar files
	 */
	public int getTotalJarFilesCount() {
		int jars = 0;
		for (DaemonConfig dc : daemonConfigs) {
			jars += dc.getTestJars().size();
		}
		return jars;
	}

	public int getTotalThreadCount() {
		return sum(daemonConfigs, on(DaemonConfig.class).getThreads());
	}

	public String getTestplanFileName() {
		return testplanFileName;
	}

	public void setTestplanFileName(final String testplanFileName) {
		this.testplanFileName = testplanFileName;
	}

	public long getLastProfileEventStartTime() {
		long startTime = 0;
		for (DaemonConfig dc : daemonConfigs) {
			for (AbstractTestplanConfig tpc : dc.getTestplanConfigs().values()) {
				LoadProfileConfig lpc = (LoadProfileConfig) tpc;
				startTime = Math.max(startTime, max(lpc.getLoadProfileEvents(), on(LoadProfileEvent.class).getStartTime()));
			}
		}
		return startTime;
	}
}
