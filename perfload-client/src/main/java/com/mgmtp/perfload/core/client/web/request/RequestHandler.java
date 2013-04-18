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
package com.mgmtp.perfload.core.client.web.request;

import java.util.UUID;

import com.mgmtp.perfload.core.client.web.http.HttpClientManager;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;

/**
 * Interface for handling requests.
 * 
 * @author rnaegele
 */
public interface RequestHandler {

	/**
	 * Handles a request.
	 * 
	 * @param httpClientManager
	 *            the {@link HttpClientManager}
	 * @param template
	 *            the request template
	 * @param requestId
	 *            the unique request id
	 * @return a response info object
	 */
	ResponseInfo execute(HttpClientManager httpClientManager, RequestTemplate template, UUID requestId) throws Exception;
}
