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
package com.mgmtp.perfload.core.client.web;

import javax.inject.Inject;
import javax.inject.Provider;

import net.jcip.annotations.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.flow.RequestFlowHandler;

/**
 * Load test driver implementation for Web load tests.
 * 
 * @author rnaegele
 */
@NotThreadSafe
public final class WebLtDriver implements LtDriver {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Provider<RequestFlowHandler> requestFlowHandlerProvider;

	/**
	 * Creates a new instance.
	 * 
	 * @param requestFlowHandlerProvider
	 *            handles a {@link RequestFlow}
	 */
	@Inject
	public WebLtDriver(final Provider<RequestFlowHandler> requestFlowHandlerProvider) {
		this.requestFlowHandlerProvider = requestFlowHandlerProvider;
	}

	/**
	 * Delegates to {@link RequestFlowHandler#execute()}.
	 */
	@Override
	public void execute() throws Exception {
		log.info("Executing test driver...");

		// We need a provider because a RequestFlowHandler might be thread-scoped
		// and gets reset after each run
		requestFlowHandlerProvider.get().execute();
	}
}
