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
package com.mgmtp.perfload.core.client.driver;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Pojo for encapsulating information on a process to be started by {@link ScriptLtDriver}.
 * 
 * @author rnaegele
 */
public class ProcessInfo {
	private final String directory;
	private final boolean freshEnvironment;
	private final Map<String, String> envVars;
	private final List<String> commands;
	private final boolean redirectProcessOutput;
	private final String logPrefix;

	/**
	 * @param directory
	 *            the working directory of the process
	 * @param envVars
	 *            a map of environment variables for the process
	 * @param commands
	 *            the list of commands that is passed to the ProcessBuilder
	 * @param redirectProcessOutput
	 *            whether the process' output is to be redirected to the perfLoad client's log file,
	 * @param logPrefix
	 *            the prefix that is to be prepended to the process' output, may be null
	 */
	public ProcessInfo(final String directory, final boolean freshEnvironment, final Map<String, String> envVars, final List<String> commands,
			final boolean redirectProcessOutput,
			final String logPrefix) {
		this.directory = directory;
		this.freshEnvironment = freshEnvironment;
		this.envVars = ImmutableMap.copyOf(envVars);
		this.commands = ImmutableList.copyOf(commands);
		this.redirectProcessOutput = redirectProcessOutput;
		this.logPrefix = logPrefix;
	}

	/**
	 * @return the working directory of the process
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @return the freshEnvironment
	 */
	public boolean isFreshEnvironment() {
		return freshEnvironment;
	}

	/**
	 * @return the map of environment variables
	 */
	public Map<String, String> getEnvVars() {
		return ImmutableMap.copyOf(envVars);
	}

	/**
	 * @return the list of commands
	 */
	public List<String> getCommands() {
		return ImmutableList.copyOf(commands);
	}

	/**
	 * @return {@code true}, if the process' output is to be redirected to perfLoad's log.
	 */
	public boolean isRedirectProcessOutput() {
		return redirectProcessOutput;
	}

	/**
	 * @return the prefix prepended to each line of the process' output if redirection to perfLoad's
	 *         log is enabled
	 */
	public String getLogPrefix() {
		return logPrefix;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
