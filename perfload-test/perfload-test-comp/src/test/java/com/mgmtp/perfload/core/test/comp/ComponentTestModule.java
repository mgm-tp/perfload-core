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
package com.mgmtp.perfload.core.test.comp;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.google.inject.util.Modules;
import com.mgmtp.perfload.core.client.config.AbstractLtModule;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.mgmtp.perfload.logging.SimpleLogger;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class ComponentTestModule extends AbstractLtModule {

	public ComponentTestModule(final PropertiesMap testplanProperties) {
		super(testplanProperties);
	}

	@Override
	protected void doConfigure() {
		install(Modules.override(new WebLtModule(testplanProperties)).with(new AbstractWebLtModule(testplanProperties) {
			@Override
			protected void doConfigureWebModule() {
				bindRequestHandler("GET").to(MockRequestHandler.class);

				// Test should always fail on any error
				bind(ErrorHandler.class).toInstance(new ErrorHandler() {
					@Override
					public void execute(final Throwable th) throws AbortionException {
						throw new AbortionException(LtStatus.ERROR, th.getMessage(), th);
					}
				});

				bind(SimpleLogger.class).toInstance(new SimpleLogger() {
					@Override
					public void open() throws IOException {
						// no-op
					}

					@Override
					public void close() {
						// no-op
					}

					@Override
					public void writeln(final String output) {
						LoggerFactory.getLogger(getClass()).info(output);
					}
				});
			}
		}));
	}

	static class MockRequestHandler implements RequestHandler {
		@Override
		public ResponseInfo execute(final RequestTemplate template, final UUID requestId) throws IOException {
			return new ResponseInfo("GET", "/foo", 200, "OK", Collections.<String, String>emptyMap(), "<html></html>".getBytes(),
					"<html></html>", "UTF-8", "text/html", System.currentTimeMillis(), new TimeInterval(), new TimeInterval(),
					UUID.randomUUID(), requestId);
		}
	}
}
