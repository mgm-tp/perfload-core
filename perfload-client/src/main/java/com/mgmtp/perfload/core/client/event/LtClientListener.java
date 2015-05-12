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
package com.mgmtp.perfload.core.client.event;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.mgmtp.perfload.core.client.config.annotations.ActiveThreads;
import com.mgmtp.perfload.core.client.config.annotations.DaemonId;
import com.mgmtp.perfload.core.client.config.annotations.ProcessId;
import com.mgmtp.perfload.core.client.util.LtContext;
import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.common.clientserver.Payload;
import com.mgmtp.perfload.core.common.clientserver.PayloadType;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.core.common.util.StatusInfo;
import com.mgmtp.perfload.core.common.util.StatusInfoType;

/**
 * <p>
 * Listener implementation for sending status events to the daemon. The daemon will forward these
 * events to the console.
 * </p>
 * <p>
 * Additionally, this class is responsible for cleaning up the thread scope after each run.
 * </p>
 * <p>
 * This listener class is registered internally by perfLoad.
 * </p>
 *
 * @author rnaegele
 */
@Singleton
@Immutable
@ThreadSafe
public final class LtClientListener implements LtProcessEventListener, LtRunnerEventListener {

	private final Client client;
	private final Provider<LtContext> contextProvider;
	private final int processId;
	private final int daemonId;
	private final Provider<Integer> activeThreadsProvider;

	/**
	 * @param client
	 *            The client to the daemon which is used to send to status events.
	 * @param contextProvider
	 *            Provider for {@link LtContext}. Since {@link LtContext} has thread scope and
	 *            {@link LtClientListener} is a {@link Singleton}, a provider must be injected in
	 *            order to avoid the widening of {@link LtClientListener}'s scope.
	 * @param processId
	 *            The id of the client process.
	 * @param daemonId
	 *            The id of the daemon.
	 * @param activeThreadsProvider
	 *            Provider for the number of concurrently active threads. A provider must be used
	 *            because the latest value must be retrieved whenever a status event is sent.
	 */
	@Inject
	public LtClientListener(final Client client, final Provider<LtContext> contextProvider, @ProcessId final int processId,
			@DaemonId final int daemonId, @ActiveThreads final Provider<Integer> activeThreadsProvider) {
		this.client = client;
		this.contextProvider = contextProvider;
		this.processId = processId;
		this.daemonId = daemonId;
		this.activeThreadsProvider = activeThreadsProvider;
	}

	// LtProcessEventListener methods

	/**
	 * Not implemented.
	 */
	@Override
	public void processStarted(final LtProcessEvent event) {
		//
	}

	/**
	 * Sends a {@link StatusInfo} object to the daemon when a process has finished.
	 */
	@Override
	public void processFinished(final LtProcessEvent event) {
		StatusInfo si = new StatusInfo.Builder(StatusInfoType.PROCESS_FINISHED, processId, daemonId)
				.error(event.getResult() != LtStatus.SUCCESSFUL)
				.build();
		sendStatus(si);
	}

	// LtRunnerEventListener methods

	/**
	 * Sends a {@link StatusInfo} object to the daemon when a test run is started.
	 */
	@Override
	public void runStarted(final LtRunnerEvent event) {
		sendRunStatus(event, StatusInfoType.RUN_STARTED);
	}

	/**
	 * Sends a {@link StatusInfo} object to the daemon when a process has finished.
	 */
	@Override
	public void runFinished(final LtRunnerEvent event) {
		sendRunStatus(event, StatusInfoType.RUN_FINISHED);
	}

	private void sendRunStatus(final LtRunnerEvent event, final StatusInfoType type) {
		LtContext context = contextProvider.get();

		StatusInfo si = new StatusInfo.Builder(type, processId, daemonId)
				.threadId(context.getThreadId())
				.operation(context.getOperation())
				.target(context.getTarget())
				.activeThreads(activeThreadsProvider.get())
				.throwable(event.getThrowable())
				.finished(type == StatusInfoType.RUN_FINISHED)
				.build();

		sendStatus(si);
	}

	private void sendStatus(final StatusInfo statusInfo) {
		Payload payload = new Payload(PayloadType.STATUS, statusInfo);
		client.sendMessage(payload);
	}
}
