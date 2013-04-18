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

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.mgmtp.perfload.core.client.web.response.ResponseInfo;

/**
 * Utility class for managing the {@link HttpClient}.
 * 
 * @author rnaegele
 */
public interface HttpClientManager {

	/**
	 * Executes the given HTTP request.
	 * 
	 * @param request
	 *            the request
	 * @param requestId
	 *            the unique request id
	 * @return the response info object wrapping the Http response
	 */
	ResponseInfo executeRequest(final HttpRequestBase request, UUID requestId) throws IOException;

	/**
	 * Executes the given HTTP request.
	 * 
	 * @param request
	 *            the request
	 * @param context
	 *            the http context
	 * @param requestId
	 *            the unique request id
	 * @return the response info object wrapping the Http response
	 */
	ResponseInfo executeRequest(final HttpRequestBase request, HttpContext context, UUID requestId) throws IOException;

	/**
	 * Shuts down the internal {@link HttpClient}, so a new is created for the next request.
	 */
	void shutdown();
}