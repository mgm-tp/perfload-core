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
package com.mgmtp.perfload.core.web.request;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.config.annotations.TargetHost;
import com.mgmtp.perfload.core.web.http.HttpClientManager;
import com.mgmtp.perfload.core.web.response.ResponseInfo;
import com.mgmtp.perfload.core.web.template.RequestTemplate;
import com.mgmtp.perfload.core.web.template.RequestTemplate.Body;

/**
 * Abstract {@link RequestHandler} base implementation for HTTP request handlers.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public class HttpRequestHandler implements RequestHandler {

	private final Provider<String> targetHostProvider;

	@Inject
	public HttpRequestHandler(@TargetHost final Provider<String> targetHostProvider) {
		this.targetHostProvider = targetHostProvider;
	}

	/**
	 * Prepends the current target's base URL to the URI if it is relative.
	 * 
	 * @param uriString
	 *            the uri
	 * @return the final URI used to make the request
	 * @throws URISyntaxException
	 *             if the given string violates RFC 2396
	 */
	protected URI createAbsoluteURI(final String uriString) throws URISyntaxException {
		URI uri = new URI(uriString);
		if (!uri.isAbsolute()) {
			String targetHost = targetHostProvider.get();
			uri = new URI(targetHost + uriString);
		}
		return uri;
	}

	@Override
	public ResponseInfo execute(final HttpClientManager httpClientManager, final RequestTemplate template, final UUID requestId) throws Exception {
		URI uri = createAbsoluteURI(template.getUri());
		List<NameValuePair> params = transformRequestParams(template.getRequestParameters());
		HttpRequestBase request = createRequest(template.getType(), uri, params, template.getBody());
		request.setHeaders(transformRequestHeaders(template.getRequestHeaders()));
		ResponseInfo responseInfo = httpClientManager.executeRequest(request, requestId);
		responseInfo.setUriAlias(template.getUriAlias());
		return responseInfo;
	}

	/**
	 * Transforms the map of parameters from the request template into a {@link NameValuePair} array
	 * for use with the {@link HttpClient}.
	 * 
	 * @param parameters
	 *            the request parameters
	 * @return the list of request parameters for the HttpClient
	 */
	protected List<NameValuePair> transformRequestParams(final SetMultimap<String, String> parameters) {
		List<NameValuePair> paramPairs = newArrayListWithCapacity(parameters.size());

		for (Entry<String, String> entry : parameters.entries()) {
			paramPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return paramPairs;
	}

	/**
	 * Transforms the map of headers from the request template into a {@link NameValuePair} array
	 * for use with the {@link HttpClient}.
	 * 
	 * @param headers
	 *            the request headers
	 * @return the array of request headers for the HttpClient
	 */
	protected Header[] transformRequestHeaders(final SetMultimap<String, String> headers) {
		Header[] headersArray = new Header[headers.size()];

		int i = 0;
		for (Entry<String, String> entry : headers.entries()) {
			headersArray[i++] = new BasicHeader(entry.getKey(), entry.getValue());
		}

		return headersArray;
	}

	/**
	 * Creates the request object.
	 * 
	 * @param type
	 *            the type of the HTTP request (GET, TRACE, DELETE, OPTIONS, HEAD, POST, PUT)
	 * @param uri
	 *            the uri
	 * @param parameters
	 *            the request parameters
	 * @param body
	 *            the request body
	 * @return the request
	 */
	protected HttpRequestBase createRequest(final String type, final URI uri, final List<NameValuePair> parameters, final Body body) throws Exception {
		HttpRequestBase request = HttpMethod.valueOf(type).create(uri);
		if (!(request instanceof HttpEntityEnclosingRequest)) {
			//  GET, TRACE, DELETE, OPTIONS, HEAD
			if (!parameters.isEmpty()) {
				String query = URLEncodedUtils.format(parameters, "UTF-8");
				URI requestURI = new URI(uri.getRawQuery() == null ? uri.toString() + '?' + query : uri.toString() + '&' + query);
				request.setURI(requestURI);
			}
		} else {
			// POST, PUT
			final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
			if (body != null) {
				// this only sets the content, header come from the request flow
				entityRequest.setEntity(new ByteArrayEntity(body.getContent()));
			} else {
				checkState(request instanceof HttpPost, "Invalid request: " + request.getMethod()
						+ ". Cannot add post parameters to this kind of request. Please check the request flow.");
				entityRequest.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
			}
		}
		return request;
	}

	/**
	 * Enum for HTTP methods.
	 */
	public enum HttpMethod {
		GET {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpGet(uri);
			}
		},
		POST {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpPost(uri);
			}
		},
		PUT {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpPut(uri);
			}
		},
		DELETE {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpDelete(uri);
			}
		},
		OPTIONS {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpOptions(uri);
			}
		},
		HEAD {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpHead(uri);
			}
		},
		TRACE {
			@Override
			public HttpRequestBase create(final URI uri) {
				return new HttpTrace(uri);
			}
		};

		public abstract HttpRequestBase create(URI uri);
	}
}
