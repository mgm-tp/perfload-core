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
package com.mgmtp.perfload.core.client.config;

import static ch.lambdaj.collection.LambdaCollections.with;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.mgmtp.perfload.core.client.LtProcessFactory;
import com.mgmtp.perfload.core.client.config.annotations.ActiveThreads;
import com.mgmtp.perfload.core.client.config.annotations.DaemonId;
import com.mgmtp.perfload.core.client.config.annotations.Layer;
import com.mgmtp.perfload.core.client.config.annotations.MeasuringLog;
import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.config.annotations.PerfLoadVersion;
import com.mgmtp.perfload.core.client.config.annotations.ProcessId;
import com.mgmtp.perfload.core.client.config.annotations.Target;
import com.mgmtp.perfload.core.client.config.annotations.TargetHost;
import com.mgmtp.perfload.core.client.config.annotations.ThreadId;
import com.mgmtp.perfload.core.client.config.scope.ThreadScope;
import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;
import com.mgmtp.perfload.core.client.driver.DummyLtDriver;
import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.driver.ProcessInfo;
import com.mgmtp.perfload.core.client.driver.ScriptLtDriver;
import com.mgmtp.perfload.core.client.event.LtClientListener;
import com.mgmtp.perfload.core.client.logging.LtResultLogger;
import com.mgmtp.perfload.core.client.runner.DefaultErrorHandler;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.runner.LtRunner;
import com.mgmtp.perfload.core.client.util.ConstantWaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.LtContext;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderUtils;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.client.util.WaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.concurrent.DelayingExecutorService;
import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.mgmtp.perfload.core.common.util.PropertiesUtils;
import com.mgmtp.perfload.core.logging.ResultLogger;
import com.mgmtp.perfload.core.logging.SimpleFileLogger;
import com.mgmtp.perfload.core.logging.SimpleLogger;

/**
 * Guice module for binding perfLoad's core classes.
 * 
 * @author rnaegele
 */
final class LtProcessModule extends AbstractLtModule {

	private static final String PERFLOAD_PROPERTIES = "perfload.utf8.props";

	private final int daemonId;
	private final int processId;
	private final Client client;

	/**
	 * Creates a new instance.
	 * 
	 * @param testplanProperties
	 *            properties set in the testplan xml file
	 * @param client
	 *            the client to talk to the daemon
	 * @param daemonId
	 *            The id of the daemon. Daemon IDs are global one-based consecutive integers.
	 * @param processId
	 *            The id of the process. Process IDs are one-based consecutive integers assigned by
	 *            daemon.
	 */
	public LtProcessModule(final PropertiesMap testplanProperties, final Client client, final int daemonId, final int processId) {
		super(testplanProperties);
		this.client = client;
		this.daemonId = daemonId;
		this.processId = processId;
	}

	@Override
	protected void doConfigure() {
		// We need a custom thread scope for things related to test runs, because each test runs in
		// its own thread. This gives us thread-local Guice singletons.
		final ThreadScope threadScope = new ThreadScope();
		bindScope(ThreadScoped.class, threadScope);
		// We also need to get a hold of the ThreadScope instance via Guice in order to be able to
		// call its cleanUp method after a thread is done. We need to do a clean-up in order to
		// avoid memory leaks.
		bind(ThreadScope.class).toInstance(threadScope);

		bind(Client.class).toInstance(client);

		// Factory interface for LtProcess
		install(new FactoryModuleBuilder().build(LtProcessFactory.class));

		bind(LtRunner.class);

		bindConstant().annotatedWith(DaemonId.class).to(daemonId);
		bindConstant().annotatedWith(ProcessId.class).to(processId);
		bindConstant().annotatedWith(Layer.class).to("client");

		// Property defaults
		bindConstant().annotatedWith(Names.named("wtm.beforeTestStartMillis")).to("0");
		bindConstant().annotatedWith(Names.named("wtm.strategy.constant.waitingTimeMillis")).to("0");

		bind(LtContext.class);
		bind(PlaceholderContainer.class).to(DefaultPlaceholderContainer.class);
		bind(WaitingTimeStrategy.class).to(ConstantWaitingTimeStrategy.class);
		bind(WaitingTimeManager.class);
		bind(ErrorHandler.class).to(DefaultErrorHandler.class);
		bind(ResultLogger.class).to(LtResultLogger.class);

		// listener for status info and ThreadScope clean-up
		bindLtProcessEventListener().to(LtClientListener.class);
		bindLtRunnerEventListener().to(LtClientListener.class);

		// default driver implementations
		bindLtDriver("dummy").to(DummyLtDriver.class);
		bindLtDriver("script").to(ScriptLtDriver.class);
	}

	/**
	 * Loads properties from the properties file {@code perfload.utf8.props} in the classpath root,
	 * if present. The file must be a UTF-8 encoded properties file. It is loaded using
	 * {@link Properties#load(java.io.Reader)}.
	 * 
	 * @return a map with properties
	 */
	@Override
	public PropertiesMap getProperties() {
		try {
			return PropertiesUtils.loadProperties(PERFLOAD_PROPERTIES, "UTF-8", false);
		} catch (IOException ex) {
			throw new IllegalStateException("Error loading properties from classpath: " + PERFLOAD_PROPERTIES, ex);
		}
	}

	@Provides
	@MeasuringLog
	@Singleton
	protected File provideMeasuringLogFile() {
		return new File(String.format("daemon-%s_process-%s_measuring.log", daemonId, processId));
	}

	@Provides
	@Singleton
	protected SimpleLogger provideSimpleLogger(@MeasuringLog final File measuringLogfile) {
		final SimpleLogger fileLogger = new SimpleFileLogger(measuringLogfile);
		try {
			fileLogger.open();
		} catch (IOException ex) {
			addError(ex);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				fileLogger.close();
			}
		});

		return fileLogger;
	}

	/**
	 * Provides the driver implementation to be used for the given operation.
	 * 
	 * @see AbstractLtModule#selectDriver(String, PropertiesMap, Map)
	 * @param operation
	 *            the operation
	 * @param properties
	 *            the properties
	 * @param drivers
	 *            a map of driver implementations
	 * @return the driver instance
	 */
	@Provides
	protected LtDriver provideDriverImplementation(@Operation final String operation, final PropertiesMap properties,
			final Map<String, LtDriver> drivers) {
		return selectDriver(operation, properties, drivers);
	}

	/**
	 * Provides the version of perfLoad as specified in the Maven pom.
	 * 
	 * @return the version string
	 */
	@Provides
	@Singleton
	@PerfLoadVersion
	protected String providePerfLoadVersion() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("com/mgmtp/perfload/core/common/version.txt");
		try {
			return IOUtils.toString(is, "UTF-8");
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read perfLoad version.", ex);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Provides the {@link DelayingExecutorService} used to run test threads. Registers a done
	 * callback with the executor service that cleans up the thread scope when a thread is done
	 * calling {@link ThreadScope#cleanUp()}.
	 * 
	 * @param threadScope
	 *            the {@link ThreadScope} instance
	 * @return the {@link DelayingExecutorService} implementation
	 */
	@Provides
	@Singleton
	protected DelayingExecutorService provideExecutorService(final ThreadScope threadScope) {
		DelayingExecutorService execService = new DelayingExecutorService();
		// Take care of thread scope clean-up whenever a task is done.
		execService.setDoneCallback(new Runnable() {
			@Override
			public void run() {
				LoggerFactory.getLogger(getClass()).info("Cleaning up thread scope...");
				threadScope.cleanUp();
			}
		});

		return execService;
	}

	@Provides
	@ActiveThreads
	protected int provideActiveThreads(final DelayingExecutorService execService) {
		return execService.getActiveCount();
	}

	/**
	 * <p>
	 * Provides a list of configured local ip addresses. If the client has multiple network
	 * interface cards, multiple ip addresses may be configured. The format is
	 * {@code ipaddress.<index>}, e. g.:
	 * </p>
	 * <p>
	 * {@code ipaddress.1=192.168.19.121}<br />
	 * {@code ipaddress.2=192.168.19.122}<br />
	 * {@code ipaddress.3=192.168.19.123}<br />
	 * {@code ipaddress.4=192.168.19.124}<br />
	 * {@code ipaddress.5=192.168.19.125}
	 * </p>
	 * 
	 * @param properties
	 *            the properties
	 * @return a list of {@link InetAddress} objects
	 */
	@Provides
	@Singleton
	protected List<InetAddress> provideLocalAddresses(final PropertiesMap properties) throws IOException {
		Collection<String> addresses = with(properties).clone().retainKeys(startsWith("ipaddress.")).values();
		List<InetAddress> result = newArrayList();

		for (String addressString : addresses) {
			InetAddress address = InetAddress.getByName(addressString);
			if (!address.isReachable(2000)) { // 2 sec
				throw new IllegalStateException("Configured IP address not reachable: " + address);
			}
			result.add(address);
		}

		return ImmutableList.copyOf(result);
	}

	/**
	 * <p>
	 * Provides the IP address the thread with the given id should use.
	 * </p>
	 * <p>
	 * The assignment of an ip address to a host configuration depends on the specified threadId.
	 * The index of the ip address in the list of ip addresses is determined as follows:
	 * </p>
	 * <p>
	 * {@code int size = ipAddresses.size();}<br />
	 * {@code int index = threadId <= size ? threadId - 1 : threadId % size;}
	 * </p>
	 * 
	 * @see #provideLocalAddresses(PropertiesMap)
	 * @param addresses
	 *            the list of {@link InetAddress} objects
	 * @param threadId
	 *            the id of the thread
	 * @return the {@link InetAddress} object, or {@code null} no addresses are configured
	 */
	@Provides
	@ThreadScoped
	protected InetAddress provideLocalAddress(final List<InetAddress> addresses, @ThreadId final int threadId) {
		if (addresses.isEmpty()) {
			return null;
		}

		int size = addresses.size();
		int index = threadId <= size ? threadId - 1 : threadId % size;

		return addresses.get(index);
	}

	/**
	 * Provides the operation of the current thread.
	 * 
	 * @param context
	 *            the context used to look up the operation.
	 * @return the operation
	 */
	@Provides
	@Operation
	protected String provideOperation(final LtContext context) {
		return context.getOperation();
	}

	/**
	 * Provides the target of the current thread.
	 * 
	 * @param context
	 *            the context used to look up the target.
	 * @return the target
	 */
	@Provides
	@Target
	protected String provideTarget(final LtContext context) {
		return context.getTarget();
	}

	/**
	 * Provides the id of the current thread.
	 * 
	 * @param context
	 *            the context used to look up the operation.
	 * @return the one-based thread id
	 */
	@Provides
	@ThreadId
	protected int provideThreadId(final LtContext context) {
		return context.getThreadId();
	}

	/**
	 * <p>
	 * Provides the host for the given target from the following property:
	 * </p>
	 * {@code target.<target>.host}
	 * 
	 * @param target
	 *            the target
	 * @param properties
	 *            the properties
	 * @return the target host
	 */
	@Provides
	@TargetHost
	@ThreadScoped
	protected String provideTargetHost(@Target final String target, final PropertiesMap properties) {
		return properties.get("target." + target + ".host");
	}

	/**
	 * Provides the process info object used to create a process for the given operation by the
	 * {@link ScriptLtDriver}. Below is an example with comments describing the properties
	 * necessary:
	 * 
	 * <pre>
	 * # the working directory for the new process
	 * operation.myOperation.procInfo.dir=/home/foo/bar
	 * 
	 * # should the process inherit the environment or get a fresh one?
	 * operation.myOperation.procInfo.freshEnvironment=true
	 * 
	 * # environment variable for the new process
	 * operation.myOperation.procInfo.envVars.APP_OPTS=-Dfoo=bar
	 * operation.myOperation.procInfo.envVars.MY_ENV_VAR=baz
	 * 
	 * # commands for the new process (starting at 1)
	 * operation.myOperation.procInfo.commands.1=./my_script.sh
	 * operation.myOperation.procInfo.commands.2=-param1
	 * operation.myOperation.procInfo.commands.3=-param2=42
	 * 
	 * # should the process' output be redirected to perfLoad's client log?
	 * operation.myOperation.procInfo.redirectProcessOutput=true
	 * 
	 * # optional prefix to be used for the process's log when log is redirected
	 * operation.myOperation.procInfo.logPrefix=myProc>
	 * </pre>
	 * 
	 * @param operation
	 *            the operation
	 * @param properties
	 *            the properties
	 * @param placeholderContainer
	 *            the placeholder container
	 * @return the process info object
	 */
	@Provides
	protected ProcessInfo provideProcessInfo(@Operation final String operation, final PropertiesMap properties,
			final PlaceholderContainer placeholderContainer) {

		String baseKey = "operation." + operation + ".procInfo";

		Converter<String, String> converter = new Converter<String, String>() {
			@Override
			public String convert(final String from) {
				return PlaceholderUtils.resolvePlaceholders(from, placeholderContainer);
			}
		};

		String directory = converter.convert(properties.get(baseKey + ".dir"));
		boolean freshEnvironment = Boolean.valueOf(converter.convert(properties.get(baseKey + ".freshEnvironment")));
		Map<String, String> envVars = with(PropertiesUtils.getSubMap(properties, baseKey + ".envVars")).convertValues(converter);
		List<String> commands = with(PropertiesUtils.getSubList(properties, baseKey + ".commands")).convert(converter);
		boolean redirectProcessOutput = Boolean.valueOf(converter.convert(properties.get(baseKey + ".redirectProcessOutput")));
		String logPrefix = converter.convert(properties.get(baseKey + ".logPrefix"));

		return new ProcessInfo(directory, freshEnvironment, envVars, commands, redirectProcessOutput, logPrefix);
	}

	@Provides
	@ThreadScoped
	protected UUID provideExecutionId() {
		return UUID.randomUUID();
	}
}
