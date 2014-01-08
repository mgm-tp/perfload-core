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
package com.mgmtp.perfload.core.client.driver;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback;
import com.mgmtp.perfload.core.common.util.LoggingGobbleCallback.Level;
import com.mgmtp.perfload.core.common.util.StreamGobbler;
import com.mgmtp.perfload.logging.ResultLogger;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * Driver implementation for running an external script. The process is started and waited for.
 * 
 * @author rnaegele
 */
public class ScriptLtDriver implements LtDriver {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Provider<ProcessInfo> processInfoProvider;
	private final Provider<ResultLogger> loggerProvider;
	private final Provider<UUID> executionIdProvider;

	@Inject
	public ScriptLtDriver(final Provider<ProcessInfo> processInfoProvider, final Provider<ResultLogger> loggerProvider,
			final Provider<UUID> executionIdProvider) {
		this.processInfoProvider = processInfoProvider;
		this.loggerProvider = loggerProvider;
		this.executionIdProvider = executionIdProvider;
	}

	/**
	 * Creates a new process using the {@link ProcessInfo} instance retrieved from the associated
	 * provider that was set in the constructor.
	 */
	@Override
	public void execute() throws Exception {
		ProcessInfo processInfo = processInfoProvider.get();

		log.info("Executing script driver...");
		log.info("Using process information: {}", processInfo);

		ProcessBuilder pb = new ProcessBuilder(processInfo.getCommands());

		String directory = processInfo.getDirectory();
		if (directory != null) {
			pb.directory(new File(directory));
		}

		Map<String, String> environment = pb.environment();
		if (processInfo.isFreshEnvironment()) {
			environment.clear();
		}

		for (Entry<String, String> envEntry : processInfo.getEnvVars().entrySet()) {
			environment.put(envEntry.getKey(), envEntry.getValue());
		}

		TimeInterval ti = new TimeInterval();
		ti.start();

		Process process = pb.start();
		log.info("External process started.");

		if (processInfo.isRedirectProcessOutput()) {
			log.info("Process output is redirected to perfLoad's log.");

			StreamGobbler gobbler = new StreamGobbler(Executors.newCachedThreadPool());
			gobbler.addStream(process.getInputStream(), "UTF-8",
					new LoggingGobbleCallback(Level.INFO, processInfo.getLogPrefix()));
			gobbler.addStream(process.getErrorStream(), "UTF-8",
					new LoggingGobbleCallback(Level.ERROR, processInfo.getLogPrefix()));
		}

		if (processInfo.isWaitFor()) {
			int exitCode = process.waitFor();
			log.info("External process terminated with exit code {}.", exitCode);
		}
		ti.stop();

		loggerProvider.get().logResult(System.currentTimeMillis(), ti, ti, "SCRIPT", null, null, executionIdProvider.get(),
				UUID.randomUUID());
	}
}
