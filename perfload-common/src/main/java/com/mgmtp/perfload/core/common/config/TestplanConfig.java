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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;

/**
 * Represents a testplan configuration.
 * 
 * @author rnaegele
 */
public class TestplanConfig {
	private final File testplanFile;
	private final ListMultimap<ProcessKey, LoadProfileEvent> loadProfileEventsByProcess;
	private final List<TestJar> testJars;
	private final Map<ProcessKey, TestConfig> testConfigs;
	private final int totalProcessCount;
	private final int totalThreadCount;
	private final long startTimeOfLastEvent;
	private final List<ProcessConfig> processConfigs;

	public TestplanConfig(final File testplanFile, final ListMultimap<ProcessKey, LoadProfileEvent> loadProfileEvents,
			final List<TestJar> testJars, final Map<ProcessKey, TestConfig> testConfigs,
			final List<ProcessConfig> processConfigs, final int totalProcessCount, final int totalThreadCount,
			final long startTimeOfLastEvent) {
		this.testplanFile = testplanFile;
		this.loadProfileEventsByProcess = loadProfileEvents;
		this.testJars = testJars;
		this.testConfigs = testConfigs;
		this.totalProcessCount = totalProcessCount;
		this.totalThreadCount = totalThreadCount;
		this.startTimeOfLastEvent = startTimeOfLastEvent;
		this.processConfigs = processConfigs;
	}

	/**
	 * @return the testplanFile
	 */
	public File getTestplanFile() {
		return testplanFile;
	}

	/**
	 * @return the loadProfileEventsByProcess
	 */
	@VisibleForTesting
	ListMultimap<ProcessKey, LoadProfileEvent> getLoadProfileEventsByProcess() {
		return loadProfileEventsByProcess;
	}

	public List<ProcessConfig> getProcessConfigs() {
		return processConfigs;
	}

	/**
	 * @return the testJars
	 */
	public List<TestJar> getTestJars() {
		return testJars;
	}

	/**
	 * @return the testConfig
	 */
	public Map<ProcessKey, TestConfig> getTestConfigs() {
		return testConfigs;
	}

	/**
	 * @return the totalProcessCount
	 */
	public int getTotalProcessCount() {
		return totalProcessCount;
	}

	/**
	 * @return the totalThreadCount
	 */
	public int getTotalThreadCount() {
		return totalThreadCount;
	}

	/**
	 * @return the startTimeOfLastEvent
	 */
	public long getStartTimeOfLastEvent() {
		return startTimeOfLastEvent;
	}
}
