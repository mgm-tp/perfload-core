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
package com.mgmtp.perfload.core.test.comp;

import static com.google.common.base.Joiner.on;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.LtProcess;
import com.mgmtp.perfload.core.common.config.ProcessConfig;
import com.mgmtp.perfload.core.daemon.util.AbstractClientRunner;

/**
 * {@link AbstractClientRunner} that does not fork separates processes but runs {@link LtProcess}
 * instances in the same VM for easier debugging.
 *
 * @author rnaegele
 */
public class InlineClientRunner extends AbstractClientRunner {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * @param execService
	 *            the {@link ExecutorService} for running client processes
	 */
	public InlineClientRunner(final ExecutorService execService) {
		super(execService);
	}

	@Override
	public Future<Integer> runClient(final File clientDir, final ProcessConfig procConfig, final List<String> arguments) {
		List<String> argsList = new ArrayList<>(arguments);
		argsList.add("-debug-inline");
		argsList.add("true");

		log.info("Running inline test process with arguments: {}", on(' ').join(argsList));

		Runnable r = () -> {
			final String[] args = new String[argsList.size()];
			LtProcess.main(argsList.toArray(args));
		};
		return execService.submit(r, 0);
	}
}
