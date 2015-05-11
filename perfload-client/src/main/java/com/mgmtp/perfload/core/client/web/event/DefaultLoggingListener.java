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
package com.mgmtp.perfload.core.client.web.event;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.logging.ResultLogger;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * Listener for logging time measurements. This listener is registered internally by perfLoad in
 * {@link WebLtModule}.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public final class DefaultLoggingListener implements RequestFlowEventListener {

	private final Provider<ResultLogger> loggerProvider;

	/**
	 * @param loggerProvider
	 *            The {@link Provider} for the {@link ResultLogger}. Since this class has
	 *            {@link Singleton} scope and the result logger may have a narrower scope, a
	 *            provider must be injected in order to avoid scope widening.
	 */
	@Inject
	public DefaultLoggingListener(final Provider<ResultLogger> loggerProvider) {
		this.loggerProvider = loggerProvider;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void beforeRequestFlow(final RequestFlowEvent event) { /* no-op */
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void afterRequestFlow(final RequestFlowEvent event) { /* no-op */
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void beforeRequest(final RequestFlowEvent event) { /* no-op */
	}

	/**
	 * Retrieves the {@link ResponseInfo} from the {@code event} and, if {@code non-null}, logs it
	 * with the current {@link ResultLogger}.
	 */
	@Override
	public void afterRequest(final RequestFlowEvent event) {
		ResponseInfo responseInfo = event.getResponseInfo();

		if (responseInfo != null) {
			String errorMsg = event.getErrorMsg();
			String type = responseInfo.getMethodType();
			TimeInterval tiBeforeBody = responseInfo.getTimeIntervalBeforeBody();
			TimeInterval tiTotal = responseInfo.getTimeIntervalTotal();
			String uri = responseInfo.getUri();
			String uriAlias = responseInfo.getUriAlias();
			if (uriAlias == null) {
				uriAlias = uri;
			}
			UUID execId = responseInfo.getExecutionId();
			UUID requestId = responseInfo.getRequestId();

			ResultLogger logger = loggerProvider.get();
			logger.logResult(errorMsg, responseInfo.getTimestamp(), tiBeforeBody, tiTotal, type, uri, uriAlias, execId, requestId);
		}
	}
}
