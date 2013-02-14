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
package com.mgmtp.perfload.core.test.comp;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mgmtp.perfload.core.clientserver.server.DefaultServer;
import com.mgmtp.perfload.core.clientserver.server.Server;
import com.mgmtp.perfload.core.clientserver.util.DaemonThreadFactory;
import com.mgmtp.perfload.core.common.config.XmlConfigReader;
import com.mgmtp.perfload.core.console.LtConsole;
import com.mgmtp.perfload.core.console.meta.LtMetaInfoHandler;
import com.mgmtp.perfload.core.console.status.FileStatusTransformer;
import com.mgmtp.perfload.core.console.status.StatusTransformer;
import com.mgmtp.perfload.core.daemon.LtDaemon;

/**
 * @author rnaegele
 */
public class ComponentTest {

	private static final int DAEMON_1_PORT = 8042;
	private static final int DAEMON_2_PORT = 8043;

	private final Logger log = LoggerFactory.getLogger(getClass());

	// For profiling with TPTP
	public static final void main(final String... args) {
		TestNG testng = new TestNG();
		testng.setTestClasses(new Class[] { ComponentTest.class });
		testng.run();
	}

	@BeforeTest
	public void startDaemons() throws IOException {
		log.debug("Starting daemons...");

		ExecutorService executorService = Executors.newFixedThreadPool(2, new DaemonThreadFactory());
		executorService.submit(new DaemonTask(DAEMON_1_PORT));
		executorService.submit(new DaemonTask(DAEMON_2_PORT));
	}

	@AfterTest
	public void shutdownDaemons() {
		log.debug("Stopping daemons...");

		LtDaemon.shutdownDaemon(DAEMON_1_PORT);
		LtDaemon.shutdownDaemon(DAEMON_2_PORT);
	}

	@Test
	public void testLoadProfile() throws Exception {
		XmlConfigReader confReader = new XmlConfigReader("src/test/resources/testplan_loadprofile.xml", "UTF-8");
		StatusTransformer transformer = new FileStatusTransformer(8, new File("ltStatus_loadprofile.txt"), new File(
				"ltThreads_loadprofile.txt"), "UTF-8");
		LtConsole console = new LtConsole(confReader.readConfig(), Executors.newCachedThreadPool(new DaemonThreadFactory()),
				transformer, new LtMetaInfoHandler(), false, false, 300000L);
		console.execute();

		assertEquals(console.isTestSuccessful(), true);
	}

	static class DaemonTask implements Callable<Void> {

		private final int port;
		private final File clientDir;

		public DaemonTask(final int port) throws IOException {
			this.port = port;
			clientDir = new File("target", "client");
			FileUtils.forceMkdir(clientDir);
			FileUtils.forceDeleteOnExit(clientDir);
		}

		@Override
		public Void call() throws Exception {
			try {
				ExecutorService executorService = Executors.newCachedThreadPool();
				Server server = new DefaultServer(port);
				LtDaemon daemon = new LtDaemon(clientDir, new InlineClientRunner(executorService), server);
				daemon.execute();
				executorService.shutdown();
				executorService.awaitTermination(10L, TimeUnit.SECONDS);
			} finally {
				FileUtils.forceDelete(clientDir);
			}
			return null;
		}
	}
}
