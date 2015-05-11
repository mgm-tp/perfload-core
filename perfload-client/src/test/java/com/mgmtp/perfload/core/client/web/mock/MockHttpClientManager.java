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

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.mgmtp.perfload.core.client.web.http.HttpClientManager;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;

/**
 * @author rnaegele
 */
public class MockHttpClientManager implements HttpClientManager {

	@Override
	public ResponseInfo executeRequest(final HttpRequestBase request, final UUID requestId) throws IOException {
		return null;
	}

	@Override
	public ResponseInfo executeRequest(final HttpRequestBase request, final HttpContext context, final UUID requestId) throws IOException {
		return null;
	}

	@Override
	public void shutdown() {/**/
	}

}
