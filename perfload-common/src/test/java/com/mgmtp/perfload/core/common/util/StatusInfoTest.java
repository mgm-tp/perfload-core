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
package com.mgmtp.perfload.core.common.util;

import static org.testng.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.common.util.StatusInfo.Builder;

/**
 * @author rnaegele
 */
public class StatusInfoTest {

	@Test
	public void testBuilder() {
		StatusInfoType type = StatusInfoType.RUN_STARTED;
		Integer processId = 1;
		Integer daemonId = 2;
		Integer threadId = 3;
		String operation = "myOperation";
		String target = "myTarget";
		Boolean error = false;
		Integer activeThreads = 9;
		String stackTrace = null;
		Exception exception = null;
		Boolean finished = false;

		StatusInfo si = new Builder(type, processId, daemonId)
				.threadId(threadId)
				.operation(operation)
				.target(target)
				.error(error)
				.activeThreads(activeThreads)
				.stackTrace(stackTrace)
				.exception(exception)
				.finished(finished)
				.build();

		assertEquals(si.getType(), type);
		assertEquals(si.getProcessId(), processId);
		assertEquals(si.getDaemonId(), daemonId);
		assertEquals(si.getThreadId(), threadId);
		assertEquals(si.getOperation(), operation);
		assertEquals(si.getTarget(), target);
		assertEquals(si.getError(), error);
		assertEquals(si.getActiveThreads(), activeThreads);
		assertEquals(si.getStackTrace(), stackTrace);
		assertEquals(si.getFinished(), finished);

		si = new Builder(si).build();

		assertEquals(si.getType(), type);
		assertEquals(si.getProcessId(), processId);
		assertEquals(si.getDaemonId(), daemonId);
		assertEquals(si.getThreadId(), threadId);
		assertEquals(si.getOperation(), operation);
		assertEquals(si.getTarget(), target);
		assertEquals(si.getError(), error);
		assertEquals(si.getActiveThreads(), activeThreads);
		assertEquals(si.getStackTrace(), stackTrace);
		assertEquals(si.getFinished(), finished);

		StringWriter sw = new StringWriter();
		exception = new Exception("some exception");
		exception.printStackTrace(new PrintWriter(sw));
		stackTrace = sw.toString();

		si = new Builder(si)
				.exception(exception)
				.build();

		assertEquals(si.getStackTrace(), stackTrace);

		assertEquals(si.toString(), LtUtils.toDefaultString(si));
	}
}
