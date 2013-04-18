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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Helper class for initializing Guice.
 * 
 * @author rnaegele
 */
public final class ModulesLoader {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Client client;
	private final int daemonId;
	private final int processId;
	private final AbstractLtModule testplanModule;
	private final PropertiesMap testplanProps;

	/**
	 * @param testplanModule
	 *            the Guice module configured in the testplan configuration file; must have a public
	 *            zero-args constructor and extend {@link AbstractLtModule}
	 * @param testplanProps
	 *            testplan properties as specified in the testplan configuration file
	 * @param client
	 *            the client for communicating with the daemon
	 * @param daemonId
	 *            the one-based id of the daemon
	 * @param processId
	 *            the one-base id of the process
	 */
	public ModulesLoader(final AbstractLtModule testplanModule, final PropertiesMap testplanProps, final Client client,
			final int daemonId, final int processId) {
		this.testplanModule = testplanModule;
		this.testplanProps = testplanProps;
		this.client = client;
		this.daemonId = daemonId;
		this.processId = processId;
	}

	/**
	 * Creates the Guice injector. Internal perfLoad module and that specified in the testplan are
	 * installed such that the latter one can override default bindings. Also binds properties as
	 * returned by {@link AbstractLtModule#getProperties()} including properties specified in the
	 * testplan configuration file which take precedence over those loaded from properties files.
	 * 
	 * @return the Guice injector
	 */
	public Injector createInjector() throws IOException {
		log.info("Creating Guice injector...");

		// Internal Guice module that is always installed
		LtProcessModule ltProcessModule = new LtProcessModule(testplanProps, client, daemonId, processId);

		PropertiesMap defaultProps = ltProcessModule.getProperties();
		PropertiesMap driverProps = testplanModule.getProperties();

		// Combine properties from driver and testplan. Testplan props take precedence.
		PropertiesMap allProps = new PropertiesMap(defaultProps);
		allProps.putAll(driverProps);
		allProps.putAll(testplanProps);

		// Create module that binds the properties.
		PropertiesModule propertiesModule = new PropertiesModule(allProps);

		log.info("Properties for test: {}", allProps);

		// Testplan module always overrides existing bindings
		Module module = Modules.override(ltProcessModule).with(testplanModule);

		// Properties in files always override those set in modules
		return Guice.createInjector(Stage.PRODUCTION, Modules.override(module).with(propertiesModule));
	}
}
