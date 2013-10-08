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
package com.mgmtp.perfload.test.utils;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.config.annotations.Target;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Guice module for driver unit tests. Wraps the actual driver module for execution in a unit test.
 * 
 * @author rnaegele
 * @since 4.7.0
 */
public class DriverTestModule extends AbstractWebLtModule {
	private final AbstractWebLtModule driverModule;
	private final String operation;
	private final String target;

	public DriverTestModule(final AbstractWebLtModule driverModule, final PropertiesMap testplanProperties,
			final String operation,
			final String target) {
		super(testplanProperties);
		this.driverModule = driverModule;
		this.operation = operation;
		this.target = target;
	}

	@Override
	protected void doConfigureWebModule() {
		bindConstant().annotatedWith(Operation.class).to(operation);
		bindConstant().annotatedWith(Target.class).to(target);

		bindRequestFlowEventListener().to(ResponseContentDumpListener.class);

		install(Modules.override(driverModule).with(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ErrorHandler.class).toInstance(new ErrorHandler() {
					@Override
					public void execute(final Throwable th) throws AbortionException {
						throw new AbortionException(LtStatus.ERROR, th.getMessage(), th);
					}
				});
			}
		}));
	}

	@Override
	public PropertiesMap getProperties() throws IOException {
		return driverModule.getProperties();
	}
}
