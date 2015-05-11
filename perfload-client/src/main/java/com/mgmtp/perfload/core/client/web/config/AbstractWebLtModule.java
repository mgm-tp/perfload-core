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
package com.mgmtp.perfload.core.client.web.config;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mgmtp.perfload.core.client.config.AbstractLtModule;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Abstract base class for Guice modules. Binds {@link LtProcessEventListener}s and
 * {@link LtRunnerEventListener}s, and provides methods for adding such listeners. Custom modules
 * for Web load tests should inherit from this class.
 * 
 * @author rnaegele
 */
public abstract class AbstractWebLtModule extends AbstractLtModule {

	private Multibinder<RequestFlowEventListener> requestFlowListeners;

	/**
	 * @param testplanProperties
	 *            properties set in the testplan xml file
	 */
	public AbstractWebLtModule(final PropertiesMap testplanProperties) {
		super(testplanProperties);
	}

	/**
	 * Creates a {@link Multibinder} for {@link RequestFlowEventListener}s and then calls
	 * {@link #doConfigureWebModule()}.
	 * 
	 * @see AbstractModule#configure()
	 */
	@Override
	protected final void doConfigure() {
		requestFlowListeners = Multibinder.newSetBinder(binder(), RequestFlowEventListener.class);
		doConfigureWebModule();
	}

	/**
	 * This method must be implemented to configure Guice bindings.
	 * 
	 * @see AbstractModule#configure()
	 */
	protected abstract void doConfigureWebModule();

	/**
	 * Creates a {@link LinkedBindingBuilder} for a {@link RequestHandler} for the specified
	 * {@code requestType}. The binding for the {@link RequestHandler} is qualified with a
	 * {@link Named} annotation using the specified {@code requestType} as {@code value} parameter.
	 * 
	 * @param requestType
	 *            the request type
	 * @see AnnotatedBindingBuilder#annotatedWith(Class)
	 */
	protected LinkedBindingBuilder<RequestHandler> bindRequestHandler(final String requestType) {
		return bind(RequestHandler.class).annotatedWith(Names.named(requestType));
	}

	/**
	 * Creates a {@link LinkedBindingBuilder} for a {@link RequestFlowEventListener}. This method
	 * should be used to add {@link RequestFlowEventListener}s to the internal {@link Multibinder}.
	 * 
	 * @see Multibinder#addBinding()
	 */
	protected final LinkedBindingBuilder<RequestFlowEventListener> bindRequestFlowEventListener() {
		return requestFlowListeners.addBinding();
	}
}
