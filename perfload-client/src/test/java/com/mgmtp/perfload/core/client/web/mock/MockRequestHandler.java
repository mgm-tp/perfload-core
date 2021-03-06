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
package com.mgmtp.perfload.core.client.web.mock;

import java.io.IOException;
import java.util.UUID;

import com.google.common.collect.ImmutableSetMultimap;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class MockRequestHandler implements RequestHandler {

	private final int statusCode;

	public MockRequestHandler(final int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public ResponseInfo execute(final RequestTemplate template, final UUID requestId) throws IOException {
		return new ResponseInfo.Builder()
				.methodType("GET")
				.uri("/foo")
				.statusCode(statusCode)
				.statusMsg("")
				.headers(ImmutableSetMultimap.of())
				.body("test content".getBytes())
				.bodyAsString("test content")
				.charset("UTF-8")
				.contentType("text/plain")
				.timestamp(System.currentTimeMillis())
				.timeIntervalBeforeBody(new TimeInterval())
				.timeIntervalTotal(new TimeInterval())
				.executionId(UUID.randomUUID())
				.requestId(requestId)
				.build();
	}
}
