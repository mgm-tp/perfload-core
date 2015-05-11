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
import java.util.stream.IntStream;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.inject.util.Modules;
import com.mgmtp.perfload.core.client.config.AbstractLtModule;
import com.mgmtp.perfload.core.client.config.annotations.TargetHost;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.mgmtp.perfload.logging.SimpleLogger;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

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
				try {
					final MockWebServer webServer = new MockWebServer();
					MockResponse mockResponse = new MockResponse()
							.addHeader("foo", "bar")
							.setResponseCode(200)
							.setHeader("Content-Type", "text/html")
							.setBody("<html></html>");

					IntStream.range(0, 80).forEach(i -> webServer.enqueue(mockResponse));
					webServer.start();
					bind(MockWebServer.class).toInstance(webServer);
					bindConstant().annotatedWith(TargetHost.class).to(webServer.getUrl("").toString());

					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							try {
								webServer.shutdown();
							} catch (IOException ex) {
								logger.error(ex.getMessage(), ex);
							}
						}
					});
				} catch (IOException ex) {
					Throwables.propagate(ex);
				}

				// Test should always fail on any error
				bind(ErrorHandler.class).toInstance(th -> {
					logger.error(th.getMessage(), th);
					throw new AbortionException(LtStatus.ERROR, th.getMessage(), th);
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
}
