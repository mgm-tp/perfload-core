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
package com.mgmtp.perfload.core.client.web.okhttp;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.config.scope.ExecutionScoped;
import com.squareup.okhttp.OkHttpClient;

/**
 * Manages a thread's OkHttpClient. The client is cached internally and released by calling
 * {@link #close()}.
 *
 * @author rnaegele
 */
@ExecutionScoped
public class OkHttpManager implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpManager.class);

	private final Provider<OkHttpClient> okHttpClientProvider;

	private OkHttpClient client;

	@Inject
	public OkHttpManager(final Provider<OkHttpClient> okHttpClientProvider) {
		this.okHttpClientProvider = okHttpClientProvider;
	}

	/**
	 * Gets the cached OkHttpClient or creates and caches a new one.
	 *
	 * @return the OkHttpclient instance
	 */
	public OkHttpClient getClient() {
		if (client == null) {
			LOGGER.info("Creating new OkHttpClient...");
			client = okHttpClientProvider.get();
		}
		return client;
	}

	/**
	 * Evitcs all connections from the client's pool and null out the client, so a new one is
	 * created for the next thread.
	 */
	@Override
	public void close() throws Exception {
		client.getConnectionPool().evictAll();
		client = null;
	}
}
