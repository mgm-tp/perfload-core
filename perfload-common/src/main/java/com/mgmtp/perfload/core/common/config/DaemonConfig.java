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

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sum;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Maps.newHashMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Comprises all configuration information a daemon process needs to know in order to run test processes.
 * 
 * @author rnaegele
 */
public class DaemonConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int daemonId;
	private final String host;
	private final int port;
	private final List<ProcessConfig> processConfigs = newArrayListWithExpectedSize(2);
	private final Map<Integer, AbstractTestplanConfig> testplanConfigs = newHashMap();
	private final List<TestJar> testJars = newArrayList();
	private final List<TestJar> jarCache;

	/**
	 * @param daemonId
	 *            the 1-based integer id of the daemon
	 * @param host
	 *            the host the daemon runs on
	 * @param port
	 *            the port the daemon runs on
	 * @param jarCache
	 *            a cache for jars in order to save memory
	 */
	public DaemonConfig(final int daemonId, final String host, final int port, final List<TestJar> jarCache) {
		checkArgument(daemonId > 0, "'daemonId' must be an integer value greater than 0");

		this.daemonId = daemonId;
		this.host = host;
		this.port = port;
		this.jarCache = newArrayList(jarCache);
	}

	public int getId() {
		return daemonId;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	/**
	 * Add a process configuration for laucnhing a test process
	 * 
	 * @param procConfig
	 *            The {@link ProcessConfig} object
	 */
	public void addProcessConfig(final ProcessConfig procConfig) {
		processConfigs.add(procConfig);
	}

	/**
	 * @return an immutable list of process configurations
	 */
	public Collection<ProcessConfig> getProcessConfigs() {
		return ImmutableList.copyOf(processConfigs);
	}

	/**
	 * @return an immutable map of test plan configurations
	 */
	public Map<Integer, AbstractTestplanConfig> getTestplanConfigs() {
		return ImmutableMap.copyOf(testplanConfigs);
	}

	/**
	 * @param processId
	 *            the process id
	 * @return the test plan configuration for the process with the specified id
	 */
	public AbstractTestplanConfig getTestplanConfig(final int processId) {
		return testplanConfigs.get(processId);
	}

	/**
	 * Adds a test plan configuration for the process with the given id
	 * 
	 * @param processId
	 *            the process id
	 * @param lpc
	 *            the test plan configuration
	 */
	public void addTestplanConfig(final int processId, final AbstractTestplanConfig lpc) {
		testplanConfigs.put(processId, lpc);
	}

	/**
	 * @return an immutable list of test jars
	 */
	public List<TestJar> getTestJars() {
		return Collections.unmodifiableList(testJars);
	}

	/**
	 * Adds a test jar.
	 * 
	 * @param name
	 *            the name of the jar file
	 * @param value
	 *            the contents of the jar file
	 */
	public void addTestJar(final String name, final byte[] value) {
		TestJar jar = new TestJar(name, value);
		int index = jarCache.indexOf(jar);
		if (index >= 0) {
			jar = jarCache.get(index);
		} else {
			jarCache.add(jar);
		}
		testJars.add(jar);
	}

	public int getThreads() {
		return sum(testplanConfigs.values(), on(AbstractTestplanConfig.class).getThreads());
	}
}
