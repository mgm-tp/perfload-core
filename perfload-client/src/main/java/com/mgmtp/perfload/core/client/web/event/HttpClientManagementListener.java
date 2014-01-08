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
package com.mgmtp.perfload.core.client.web.event;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.client.web.http.HttpClientManager;

/**
 * Event listener for shutting down the {@link HttpClientManager}. After each run, a shutdown is
 * performed, so each request flow execution gets a new {@link HttpClient}. This listener is
 * registered internally by perfLoad in {@link WebLtModule}.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public final class HttpClientManagementListener implements LtRunnerEventListener {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Provider<HttpClientManager> httpClientManagerProvider;

	/**
	 * @param httpClientManagerProvider
	 *            the {@link HttpClientManager} provider
	 */
	@Inject
	public HttpClientManagementListener(final Provider<HttpClientManager> httpClientManagerProvider) {
		this.httpClientManagerProvider = httpClientManagerProvider;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void runStarted(final LtRunnerEvent event) {
		// no-op
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void runFinished(final LtRunnerEvent event) {
		log.info("Closing HttpClient connections...");
		httpClientManagerProvider.get().shutdown();
	}
}
