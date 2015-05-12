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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.common.config.ProcessConfig;
import com.mgmtp.perfload.core.common.util.StreamGobbler;

/**
 * Unit test for {@link ForkedProcessClientRunner}.
 *
 * @author rnaegele
 */
public class ForkedProcessClientRunnerTest {

	@Mock
	private Future<Integer> result;

	@Mock
	private ExecutorService executor;

	@Captor
	private ArgumentCaptor<Callable<Integer>> callableCaptor;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testProcessExecution() throws InterruptedException, ExecutionException {
		when(executor.submit(callableCaptor.capture())).thenReturn(result);

		StreamGobbler gobbler = new StreamGobbler(executor);
		ForkedProcessClientRunner fpcr = new ForkedProcessClientRunner(executor, gobbler);
		ProcessConfig proConf = new ProcessConfig(1, 1, ImmutableList.<String>of());

		// Start the process and wait for it
		fpcr.runClient(new File("."), proConf, ImmutableList.<String>of()).get();

		verify(executor).submit(callableCaptor.getValue());
	}
}
