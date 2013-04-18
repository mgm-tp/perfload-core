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
package com.mgmtp.perfload.core.client.web.http;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import net.jcip.annotations.NotThreadSafe;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * Default {@link HttpClientManager} implementation. This class lazily creates an HttpClient
 * instance and caches it internally until {@link #shutdown()} is called.
 * 
 * @author rnaegele
 */
@ThreadScoped
@NotThreadSafe
public final class DefaultHttpClientManager implements HttpClientManager {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String EXECUTION_ID_HEADER = "X-perfLoad-Execution-Id";
	private static final String OPERATION_HEADER = "X-perfLoad-Operation";
	private static final String REQUEST_ID_HEADER = "X-perfLoad-Request-Id";

	private final Provider<HttpClient> httpClientProvider;
	private final UUID executionId;
	private final String operation;

	private HttpClient httpClient;

	/**
	 * Constructs a new {@link DefaultHttpClientManager}.
	 * 
	 * @param httpClientProvider
	 *            the provider for the HttpClient implementation
	 */
	@Inject
	public DefaultHttpClientManager(final Provider<HttpClient> httpClientProvider, final UUID executionId,
			@Operation final String operation) {
		this.httpClientProvider = httpClientProvider;
		this.executionId = executionId;
		this.operation = operation;
	}

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			log.info("Creating new HttpClient...");
			httpClient = httpClientProvider.get();
		}
		return httpClient;
	}

	/**
	 * Delegates to {@link #executeRequest(HttpRequestBase, HttpContext, UUID)}.
	 */
	@Override
	public ResponseInfo executeRequest(final HttpRequestBase request, final UUID requestId) throws IOException {
		return executeRequest(request, null, requestId);
	}

	/**
	 * Executes an HTTP request using the internal {@link HttpClient} instance encapsulating the
	 * Http response in the returns {@link ResponseInfo} object. This method takes to time
	 * measurements around the request execution, one after calling
	 * {@link HttpClient#execute(org.apache.http.client.methods.HttpUriRequest, HttpContext)} (~
	 * first-byte measurment) and the other one later after the complete response was read from the
	 * stream. These measurements are return as properties of the {@link ResponseInfo} object.
	 */
	@Override
	public ResponseInfo executeRequest(final HttpRequestBase request, final HttpContext context, final UUID requestId)
			throws IOException {
		request.addHeader(EXECUTION_ID_HEADER, executionId.toString());
		request.addHeader(OPERATION_HEADER, operation);
		request.addHeader(REQUEST_ID_HEADER, requestId.toString());

		String uri = request.getURI().toString();
		String type = request.getMethod();

		TimeInterval tiBeforeBody = new TimeInterval();
		TimeInterval tiTotal = new TimeInterval();

		tiBeforeBody.start();
		tiTotal.start();
		long timestamp = System.currentTimeMillis();

		HttpResponse response = getHttpClient().execute(request, context);
		tiBeforeBody.stop();

		// This actually downloads the response body:
		HttpEntity entity = response.getEntity();
		byte[] body = EntityUtils.toByteArray(entity);
		tiTotal.stop();

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String statusMsg = statusLine.getReasonPhrase();
		String responseCharset = EntityUtils.getContentCharSet(entity);
		String contentType = EntityUtils.getContentMimeType(entity);

		Header[] headers = response.getAllHeaders();
		Map<String, String> responseHeaders = newHashMapWithExpectedSize(headers.length);
		for (Header header : headers) {
			responseHeaders.put(header.getName(), header.getValue());
		}

		return new ResponseInfo(type, uri, statusCode, statusMsg, responseHeaders, body, responseCharset, contentType,
				timestamp, tiBeforeBody, tiTotal, executionId, requestId);
	}

	/**
	 * Shutsdown the internal {@link HttpClient} and removes the cached instance.
	 */
	@Override
	public void shutdown() {
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
	}
}
