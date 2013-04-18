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

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * Custom {@link RedirectStrategy} that not only redirects after HEAD and GET request as the HTTP
 * spec suggests. Browsers automatically redirect after POST as well, which is quite common.
 * PerfLoad should be able to do this as well.
 * 
 * @author rnaegele
 */
public class RedirectAfterPostStrategy extends DefaultRedirectStrategy {

	@Override
	public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws ProtocolException {
		boolean isRedirected = super.isRedirected(request, response, context);

		int statusCode = response.getStatusLine().getStatusCode();
		switch (statusCode) {
			case HttpStatus.SC_MOVED_TEMPORARILY:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_TEMPORARY_REDIRECT:
				return response.getFirstHeader("location") != null;
			default:
				return isRedirected;
		}
	}
}
