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
package com.mgmtp.perfload.core.web.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import com.mgmtp.perfload.core.logging.TimeInterval;
import com.mgmtp.perfload.core.web.http.HttpClientManager;
import com.mgmtp.perfload.core.web.request.RequestHandler;
import com.mgmtp.perfload.core.web.response.ResponseInfo;
import com.mgmtp.perfload.core.web.template.RequestTemplate;

/**
 * @author rnaegele
 */
public class MockRequestHandler implements RequestHandler {

	private final int statusCode;

	public MockRequestHandler(final int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public ResponseInfo execute(final HttpClientManager httpClientManager, final RequestTemplate template, final UUID requestId) throws IOException {
		return new ResponseInfo("GET", "/foo", statusCode, "", Collections.<String, String>emptyMap(), "test content".getBytes(),
				"UTF-8", "text/plain", System.currentTimeMillis(), new TimeInterval(), new TimeInterval(), UUID.randomUUID(), requestId);
	}
}
