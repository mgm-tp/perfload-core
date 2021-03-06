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
package com.mgmtp.perfload.core.client.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Abstract base class for Guice modules. Binds {@link LtProcessEventListener}s and
 * {@link LtRunnerEventListener}s, and provides methods for adding such listeners.
 * 
 * @author rnaegele
 */
public abstract class AbstractLtModule extends AbstractModule {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Multibinder<LtProcessEventListener> ltProcessListeners;
	private Multibinder<LtRunnerEventListener> ltRunnerListeners;
	private MapBinder<String, LtDriver> ltDrivers;
	private MapBinder<String, DriverSelectionPredicate> ltDriverPredicates;

	protected final PropertiesMap testplanProperties;

	/**
	 * @param testplanProperties
	 *            properties set in the testplan xml file
	 */
	public AbstractLtModule(final PropertiesMap testplanProperties) {
		this.testplanProperties = testplanProperties;
	}

	/**
	 * Creates {@link Multibinder}s for {@link LtProcessEventListener} and
	 * {@link LtRunnerEventListener} and then calls {@link #doConfigure()}.
	 * 
	 * @see AbstractModule#configure()
	 */
	@Override
	protected final void configure() {
		ltProcessListeners = Multibinder.newSetBinder(binder(), LtProcessEventListener.class);
		ltRunnerListeners = Multibinder.newSetBinder(binder(), LtRunnerEventListener.class);
		ltDrivers = MapBinder.newMapBinder(binder(), String.class, LtDriver.class);
		ltDriverPredicates = MapBinder.newMapBinder(binder(), String.class, DriverSelectionPredicate.class);
		doConfigure();
	}

	/**
	 * This method must be implemented to configure Guice bindings.
	 * 
	 * @see AbstractModule#configure()
	 */
	protected abstract void doConfigure();

	/**
	 * Returns a map of properties for the test. This method is called by perfLoad before creating
	 * the Guice {@link Injector}. The returned map is merged with the properties configured in the
	 * testplan xml file while the latter ones take precedence. The complete map is bound as a whole
	 * and additionally every single property seperately so that injection with e. g.
	 * {@code @Named("myProperty")} is possible.
	 * 
	 * @return a map of properties
	 */
	@SuppressWarnings("unused")
	public PropertiesMap getProperties() throws IOException {
		return new PropertiesMap();
	}

	/**
	 * Binds a {@link LtProcessEventListener}.
	 * 
	 * @see Multibinder#addBinding()
	 */
	protected final LinkedBindingBuilder<LtProcessEventListener> bindLtProcessEventListener() {
		return ltProcessListeners.addBinding();
	}

	/**
	 * Binds a {@link LtRunnerEventListener}.
	 * 
	 * @see Multibinder#addBinding()
	 */
	protected final LinkedBindingBuilder<LtRunnerEventListener> bindLtRunnerEventListener() {
		return ltRunnerListeners.addBinding();
	}

	/**
	 * Binds a {@link LtDriver}.
	 * 
	 * @see MapBinder#addBinding(Object)
	 */
	protected final DriverBindingBuilder bindLtDriver(final String driverKey) {
		return new DriverBindingBuilder(driverKey, ltDrivers, ltDriverPredicates);
	}
}
