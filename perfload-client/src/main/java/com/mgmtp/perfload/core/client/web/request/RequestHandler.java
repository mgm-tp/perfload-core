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
package com.mgmtp.perfload.core.client.web.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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
	 * @param template
	 *            the request template
	 * @param requestId
	 *            the unique request id
	 * @return a response info object
	 */
	ResponseInfo execute(RequestTemplate template, UUID requestId) throws Exception;

	/**
	 * Creates a URI contatenating the specified {@code base} and {@code relativeUri}.
	 *
	 * @param base
	 *            the URI base
	 * @param relativeUri
	 *            the uri
	 * @return the final URI used to make the request
	 * @throws URISyntaxException
	 *             if the given string violates RFC 2396
	 */
	default URI createUri(final String base, final String relativeUri) throws URISyntaxException {
		URI uri = new URI(relativeUri);
		if (!uri.isAbsolute()) {
			String uriBase = base;
			if (!uriBase.endsWith("/") && !relativeUri.startsWith("/")) {
				uriBase += "/";
			}
			uri = new URI(uriBase + relativeUri);
		}
		return uri;
	}
}
