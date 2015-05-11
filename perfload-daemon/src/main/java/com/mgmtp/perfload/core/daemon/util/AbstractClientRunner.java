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
package com.mgmtp.perfload.core.daemon.util;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.mgmtp.perfload.core.common.config.ProcessConfig;

/**
 * Base class for running client processes.
 * 
 * @author rnaegele
 */
public abstract class AbstractClientRunner {

	protected final ExecutorService execService;

	/**
	 * @param execService
	 *            the {@link ExecutorService} for running client processes
	 */
	public AbstractClientRunner(final ExecutorService execService) {
		this.execService = execService;
	}

	/**
	 * Asynchronously runs a client process.
	 * 
	 * @param clientDir
	 *            the client's installation directory
	 * @param procConfig
	 *            the {@link ProcessConfig} object
	 * @param arguments
	 *            command-line arguemtns for the process
	 * @return a future representing the process' exit code
	 */
	public abstract Future<Integer> runClient(File clientDir, ProcessConfig procConfig, List<String> arguments);

}
