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
package com.mgmtp.perfload.core.client.logging;

import java.net.InetAddress;

import javax.inject.Inject;

import com.mgmtp.perfload.core.client.config.annotations.DaemonId;
import com.mgmtp.perfload.core.client.config.annotations.Layer;
import com.mgmtp.perfload.core.client.config.annotations.Nullable;
import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.config.annotations.ProcessId;
import com.mgmtp.perfload.core.client.config.annotations.Target;
import com.mgmtp.perfload.core.client.config.annotations.ThreadId;
import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;
import com.mgmtp.perfload.logging.DefaultResultLogger;
import com.mgmtp.perfload.logging.ResultLogger;
import com.mgmtp.perfload.logging.SimpleLogger;

/**
 * {@link ResultLogger} implementation for logging test results in perfLoad tests. Extends
 * {@link DefaultResultLogger} with Guice-specific annotations.
 * <p>
 * Note: This class must not be shared across threads.
 * 
 * @author rnaegele
 */
@ThreadScoped
public class LtResultLogger extends DefaultResultLogger {

	/**
	 * @param localAddress
	 *            the local address of the client, or {@code null} if no specific address is used
	 * @param layer
	 *            some identifier for the layer in which the result is logged (e. g. client, server,
	 *            ...)
	 * @param operation
	 *            the operation of the current test thread
	 * @param target
	 *            the target of the current test thread
	 * @param daemonId
	 *            the daemon id
	 * @param processId
	 *            the process id
	 * @param threadId
	 *            the id of the current test thread
	 */
	@Inject
	public LtResultLogger(final SimpleLogger fileLogger, @Nullable final InetAddress localAddress, @Layer final String layer,
			@Operation final String operation, @Target final String target, @DaemonId final int daemonId,
			@ProcessId final int processId,
			@ThreadId final int threadId) {
		super(fileLogger, localAddress, layer, operation, target, daemonId, processId, threadId);
	}
}
