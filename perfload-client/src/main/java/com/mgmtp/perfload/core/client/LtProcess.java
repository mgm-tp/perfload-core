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
package com.mgmtp.perfload.core.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import com.mgmtp.perfload.core.client.config.AbstractLtModule;
import com.mgmtp.perfload.core.client.config.ModulesLoader;
import com.mgmtp.perfload.core.client.config.annotations.DaemonId;
import com.mgmtp.perfload.core.client.config.annotations.ProcessId;
import com.mgmtp.perfload.core.client.config.scope.ExecutionScope;
import com.mgmtp.perfload.core.client.event.LtProcessEvent;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.lang.LocalFirstClassLoader;
import com.mgmtp.perfload.core.client.runner.LtRunner;
import com.mgmtp.perfload.core.client.util.LtContext;
import com.mgmtp.perfload.core.client.util.concurrent.DelayingExecutorService;
import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.clientserver.client.ClientMessageListener;
import com.mgmtp.perfload.core.clientserver.client.DefaultClient;
import com.mgmtp.perfload.core.common.clientserver.Payload;
import com.mgmtp.perfload.core.common.clientserver.PayloadType;
import com.mgmtp.perfload.core.common.config.LoadProfileEvent;
import com.mgmtp.perfload.core.common.config.ProcessKey;
import com.mgmtp.perfload.core.common.config.TestConfig;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.core.common.util.MemoryInfo;
import com.mgmtp.perfload.core.common.util.MemoryInfo.Unit;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Represents a test process and runs the test threads associated with this process.
 *
 * @author rnaegele
 */
public final class LtProcess implements ClientMessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(LtProcess.class);

	private final int processId;
	private final int daemonId;
	private final Set<LtProcessEventListener> listeners;
	private final Provider<LtRunner> ltRunnerProvider;
	private final DelayingExecutorService execService;
	private final Client daemonClient;
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch exitLatch = new CountDownLatch(1);
	private final TestConfig config;
	private final ExecutionScope executionScope;

	private volatile boolean aborted = false;

	/**
	 * @param processId
	 *            The id of the process
	 * @param daemonId
	 *            The id of the daemon this process is associated with
	 * @param ltRunnerProvider
	 *            Provider for creating {@link LtRunner} instances
	 * @param listeners
	 *            {@link LtProcessEventListener} to react on {@link LtProcessEvent}s triggered by
	 *            this class
	 * @param execService
	 *            The executor service for running {@link LtRunner} instances (i. e. test threads)
	 * @param daemonClient
	 *            {@link Client} that talks to the daemon
	 * @param config
	 *            the test configuration for the test process
	 * @param executionScope
	 *            the Guice scope for executions
	 */
	@Inject
	protected LtProcess(@ProcessId final int processId, @DaemonId final int daemonId, final Provider<LtRunner> ltRunnerProvider,
			final Set<LtProcessEventListener> listeners, final DelayingExecutorService execService, final Client daemonClient,
			@Assisted final TestConfig config, final ExecutionScope executionScope) {
		this.processId = processId;
		this.daemonId = daemonId;
		this.ltRunnerProvider = ltRunnerProvider;
		this.listeners = listeners;
		this.execService = execService;
		this.daemonClient = daemonClient;
		this.config = config;
		this.executionScope = executionScope;
	}

	private List<TestInfo> setUp() {
		daemonClient.addClientMessageListener(this);

		List<LoadProfileEvent> events = config.getLoadProfileEvents();
		int eventCount = events.size();
		LOG.info("Number of load profile events for this process: {}", eventCount);

		List<TestInfo> result = newArrayListWithCapacity(eventCount);
		for (LoadProfileEvent event : events) {
			result.add(new TestInfo(event.getOperation(), event.getTarget(), event.getStartTime()));
		}

		return result;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
		Payload payload = (Payload) e.getMessage();
		switch (payload.getPayloadType()) {
			case START:
				startLatch.countDown();
				break;
			case ABORT:
				LOG.info("Test aborted. Interrupting running tasks...");
				aborted = true;
				while (startLatch.getCount() > 0) {
					startLatch.countDown();
				}
				execService.shutdownNow();
				break;
			case TEST_PROC_DISCONNECTED:
				exitLatch.countDown();
				break;
			default:
				// Other types not relevant on client side
		}
	}

	/**
	 * Schedules all test threads associated with this process.
	 *
	 * @return The overall status of this process after termination.
	 */
	protected LtStatus execute() {
		List<TestInfo> testInfoList = null;
		LtStatus result = LtStatus.SUCCESSFUL;

		try {
			testInfoList = setUp();
			daemonClient.sendMessage(new Payload(PayloadType.TEST_PROC_READY, processId));
			if (!startLatch.await(10L, TimeUnit.SECONDS)) {
				LOG.warn("Timeout awaiting start latch.");
				result = LtStatus.ERROR;
				return result;
			}

			if (aborted) {
				result = LtStatus.ERROR;
				return result;
			}
		} catch (InterruptedException ex) {
			LOG.error(ex.getMessage(), ex);
			result = LtStatus.INTERRUPTED;
			return result;
		}

		try {
			fireProcessStarted();
			for (int i = 0; i < testInfoList.size(); ++i) {
				final TestInfo ti = testInfoList.get(i);
				final int threadId = i + 1; // one-based

				final long scheduledStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) + ti.getStartTime();

				Runnable runnerWrapper = () -> {
					// Create instance for the current execution and populate it.
					LtContext context = new LtContext();
					context.setOperation(ti.getOperation());
					context.setTarget(ti.getTarget());
					context.setThreadId(threadId);

					try {
						// Add context to the executions scopeCache, so the correct instances may be injected later on.
						// LtContext is bound in ExecutionScope!
						final Map<Key<?>, Object> scopeCache = new HashMap<>();
						scopeCache.put(Key.get(LtContext.class), context);

						executionScope.enterScope(context.getExecutionId(), scopeCache);

						LtRunner testRunner = ltRunnerProvider.get();

						long actualStartTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
						LOG.info("Execution time delta (actualStartTime - scheduledStartTime): {} - {} = {}", new Object[] {
								actualStartTime, scheduledStartTime, actualStartTime - scheduledStartTime });
						LOG.info("Thread pool status [activeCount={}, poolSize={}, largestPoolSize={}]",
								new Object[] { execService.getActiveCount(), execService.getPoolSize(),
										execService.getLargestPoolSize() });

						testRunner.execute();
					} finally {
						executionScope.exitScope(context.getExecutionId());
					}
				};
				execService.schedule(runnerWrapper, ti.getStartTime(), TimeUnit.MILLISECONDS);
			}

			final int taskCount = testInfoList.size();
			Callable<LtStatus> poller = () -> {
				try {
					for (int i = 0; i < taskCount; ++i) {
						try {
							execService.takeNextCompleted().get();
						} catch (CancellationException ex1) {
							// Cannot happen because we do not cancel tasks
							LOG.error(ex1.getMessage(), ex1);
						} catch (ExecutionException ex2) {
							Throwable cause = ex2.getCause();
							LOG.error(cause.getMessage(), cause);
							if (cause instanceof AbortionException) {
								AbortionException abex = (AbortionException) cause;
								if (abex.getStatus() == LtStatus.ERROR) {
									daemonClient.sendMessage(new Payload(PayloadType.ERROR));
								}
								execService.shutdownNow();
								execService.awaitTermination(30L, TimeUnit.SECONDS);
								return abex.getStatus();
							}
						} finally {
							LOG.info("{} out of {} executions completed.", i + 1, taskCount);
							LOG.debug(MemoryInfo.getMemoryInfo(Unit.KIBIBYTES));
						}
					}
					return LtStatus.SUCCESSFUL;

				} finally {
					LOG.debug("Disconnecting...");
					daemonClient.sendMessage(new Payload(PayloadType.TEST_PROC_DISCONNECTED, new ProcessKey(processId, daemonId)));
					LOG.debug("Disconnected from daemon. Awaiting exit latch...");
					try {
						exitLatch.await(5L, TimeUnit.SECONDS);
					} catch (InterruptedException ex3) {
						//
					}
					LOG.debug("Finished status polling.");
				}
			};
			if (!execService.isShutdown()) {
				LOG.debug("Schedule status poller...");
				result = execService.schedule(poller, 30L, TimeUnit.SECONDS).get();
				LOG.debug("Polling result: {}", result);
				execService.shutdownNow();
				execService.awaitTermination(30L, TimeUnit.SECONDS);
				LOG.debug("Test process finished.");
			}
			return result;
		} catch (InterruptedException ex) {
			return result = LtStatus.INTERRUPTED;
		} catch (ExecutionException ex) {
			LOG.error(ex.getMessage(), ex);
			result = LtStatus.ERROR;
			return result;
		} finally {
			fireProcessFinished(result);
		}
	}

	private void fireProcessStarted() {
		LtProcessEvent event = new LtProcessEvent(processId, daemonId);
		LOG.debug("fireProcessStarted: {}", event);

		for (LtProcessEventListener listener : listeners) {
			LOG.debug("Executing listener: {}", listener);
			listener.processStarted(event);
		}
	}

	private void fireProcessFinished(final LtStatus result) {
		LtProcessEvent event = new LtProcessEvent(processId, daemonId, result);
		LOG.debug("fireProcessFinished: {}", event);

		for (LtProcessEventListener listener : listeners) {
			LOG.debug("Executing listener: {}", listener);
			listener.processFinished(event);
		}
	}

	static class TestInfo {
		private final String operation;
		private final String target;
		private final long startTime;

		TestInfo(final String operation, final String target, final long startTime) {
			this.operation = operation;
			this.target = target;
			this.startTime = startTime;
		}

		public String getOperation() {
			return operation;
		}

		public String getTarget() {
			return target;
		}

		public long getStartTime() {
			return startTime;
		}
	}

	public static void main(final String[] args) {
		LOG.info("Initializing test process...");

		Integer processId = null;
		Integer daemonId = null;
		int daemonPort = -1;
		File testLibDir = null;
		String[] testJarNames = null;
		boolean debug = false;
		try {
			for (int i = 0; i < args.length; ++i) {
				if (args[i].equals("-processId")) {
					processId = Integer.valueOf(args[++i]);
					continue;
				}
				if (args[i].equals("-daemonId")) {
					daemonId = Integer.valueOf(args[++i]);
					continue;
				}
				if (args[i].equals("-daemonPort")) {
					daemonPort = Integer.parseInt(args[++i]);
					continue;
				}
				if (args[i].equals("-testLibDir")) {
					testLibDir = new File(args[++i]);
					continue;
				}
				if (args[i].equals("-testJars")) {
					testJarNames = args[++i].split(";");
					continue;
				}
				if (args[i].equals("-debug-inline")) {
					debug = Boolean.parseBoolean(args[++i]);
					continue;
				}
			}
			checkArgument(processId != null && daemonId != null && daemonPort != -1);
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			ex.printStackTrace();
			System.out.println();
			printUsage();
			if (!debug) {
				System.exit(-1);
			}
		}

		final CountDownLatch propsLatch = new CountDownLatch(1);
		MessageListener listener = new MessageListener(propsLatch, daemonId, processId);

		Client client = new DefaultClient("testproc" + processId, "localhost", daemonPort);
		LOG.debug("Creating daemon client: {}", client);

		client.addClientMessageListener(listener);
		client.connect();
		client.sendMessage(new Payload(PayloadType.TEST_PROC_CONNECTED, processId));

		int exitCode = 0;
		try {
			if (!propsLatch.await(2L, TimeUnit.MINUTES)) {
				throw new TimeoutException("Timeout waiting for properties.");
			}

			TestConfig config = listener.getConfig();
			client.removeClientMessageListener(listener);

			URL[] classpathUrls;

			if (testLibDir != null && testJarNames != null) {
				int length = testJarNames.length;
				classpathUrls = new URL[length];
				URL testLibDirUrl = testLibDir.toURI().toURL();
				for (int index = 0; index < length; ++index) {
					try {
						classpathUrls[index] = new URL(testLibDirUrl, testJarNames[index]);
					} catch (MalformedURLException ex) {
						// can't really happen
						throw new IllegalStateException(ex);
					}
				}
			} else {
				classpathUrls = new URL[] {};
			}

			ClassLoader loader = new LocalFirstClassLoader(classpathUrls);
			Thread.currentThread().setContextClassLoader(loader);

			String moduleClassName = config.getGuiceModule();
			Class<?> moduleClass = Class.forName(moduleClassName, true, loader);
			checkState(AbstractLtModule.class.isAssignableFrom(moduleClass), "'" + moduleClassName + "' must extend '"
					+ AbstractLtModule.class.getName() + "'.");

			Constructor<? extends AbstractLtModule> constructor = moduleClass.asSubclass(AbstractLtModule.class)
					.getConstructor(PropertiesMap.class);
			PropertiesMap testplanProperties = config.getProperties();
			AbstractLtModule testplanModule = constructor.newInstance(testplanProperties);
			ModulesLoader modulesLoader = new ModulesLoader(testplanModule, testplanProperties, client, daemonId, processId);
			Injector injector = modulesLoader.createInjector();

			LtProcessFactory procFac = injector.getInstance(LtProcessFactory.class);
			LtProcess proc = procFac.create(config);
			LtStatus status = proc.execute();

			LOG.info(status.getMsg());

		} catch (Exception ex) {
			client.sendMessage(new Payload(PayloadType.ERROR));
			LOG.error(ex.getMessage(), ex);
			exitCode = 1;
		} finally {
			client.disconnect();
		}
		if (!debug) {
			System.exit(exitCode);
		}
	}

	static class MessageListener implements ClientMessageListener {
		private volatile TestConfig config;
		private final CountDownLatch latch;
		private final Integer daemonId;
		private final Integer processId;

		public MessageListener(final CountDownLatch latch, final Integer daemonId, final Integer processId) {
			this.latch = latch;
			this.daemonId = daemonId;
			this.processId = processId;
		}

		@Override
		public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
			Payload payload = (Payload) e.getMessage();
			switch (payload.getPayloadType()) {
				case ABORT:
					LOG.info("Test aborted.");
					e.getChannel().write(new Payload(PayloadType.TEST_PROC_DISCONNECTED, new ProcessKey(processId, daemonId)));
					e.getChannel().close().awaitUninterruptibly(30L, TimeUnit.SECONDS);
					System.exit(-1);
					break;
				case CONFIG:
					config = payload.getContent();
					latch.countDown();
					break;
				default:
					// Other types not relevant
			}
		}

		public TestConfig getConfig() {
			return config;
		}
	}

	private static void printUsage() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("Usage LtProcess:\n");
		sb.append("-processId <ID>     ID of this process; must be an integer (required).\n");
		sb.append("-daemonId <ID>      ID of the daemon this process is associated with;\n");
		sb.append("                    must be an integer (required).\n");
		sb.append("-daemonPort <port>  The port of the daemon this process is associated with (required).\n");
		sb.append("-testLibDir <dir>   Directory where the jars for the testplan dwell in (required).\n");
		sb.append("-testJars <jars>    A semi-colon-separated list of jar files names for the testplan (required).\n");
		System.out.println(sb.toString());
	}
}
