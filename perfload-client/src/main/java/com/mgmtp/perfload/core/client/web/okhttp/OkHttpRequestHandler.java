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
package com.mgmtp.perfload.core.client.web.okhttp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.squareup.okhttp.internal.Util;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang3.text.StrBuilder;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.config.annotations.ExecutionId;
import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.config.annotations.TargetHost;
import com.mgmtp.perfload.core.client.web.constants.WebConstants;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.logging.TimeInterval;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

/**
 * A {@link RequestHandler} that uses <a href="http://square.github.io/okhttp/">OkHttp</a> as the
 * underlying HTTP client.
 *
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public class OkHttpRequestHandler implements RequestHandler {

	private static enum HttpMethod {
		GET,
		POST,
		PUT,
		DELETE,
		OPTIONS,
		HEAD,
		TRACE;
	}

	private final Provider<OkHttpManager> okHttpClientManagerProvider;
	private final Provider<String> targetHostProvider;
	private final Provider<UUID> executionIdProvider;
	private final Provider<String> operationProvider;
	private final Provider<Builder> requestBuilderProvider;

	@Inject
	public OkHttpRequestHandler(final Provider<OkHttpManager> okHttpClientManagerProvider, @TargetHost final Provider<String> targetHostProvider,
			@ExecutionId final Provider<UUID> executionIdProvider, @Operation final Provider<String> operationProvider,
			final Provider<Request.Builder> requestBuilderProvider) {
		this.okHttpClientManagerProvider = okHttpClientManagerProvider;
		this.targetHostProvider = targetHostProvider;
		this.executionIdProvider = executionIdProvider;
		this.operationProvider = operationProvider;
		this.requestBuilderProvider = requestBuilderProvider;
	}

	@Override
	public ResponseInfo execute(final RequestTemplate template, final UUID requestId) throws Exception {
		URI uri = createUri(targetHostProvider.get(), template.getUri());
		String method = template.getType();

		Request request = prepareRequest(uri, method, template, requestId);
		Call call = okHttpClientManagerProvider.get().getClient().newCall(request);

		TimeInterval tiBeforeBody = new TimeInterval();
		TimeInterval tiTotal = new TimeInterval();

		tiBeforeBody.start();
		tiTotal.start();
		long timestamp = System.currentTimeMillis();

		Response response = call.execute();

		tiBeforeBody.stop();

		int statusCode = response.code();
		String statusMsg = response.message();

		try (ResponseBody body = response.body()) {
			MediaType contentType = body != null ? body.contentType() : null;
			String contentTypeString = contentType != null ? contentType.toString() : null;
			Charset charset = contentType!=null?contentType.charset(Util.UTF_8):Util.UTF_8;
			String responseCharset = charset != null ? charset.name() : null;
			byte[] bodyBytes = body != null ? body.bytes() : null;
			String bodyAsString = null;
			if  (contentType == null 
					|| contentType.subtype().equals("json") 
					|| contentType.type().equals("application") && (contentType.subtype().equals("octet-stream") || contentType.subtype().equals("elster-payloadcontainer"))
					|| contentType.type().equals("text") && !contentType.subtype().equals("javascript") && !contentType.subtype().equals("css")) {
				bodyAsString = bodyAsString(bodyBytes, responseCharset);
			}
			if (responseCharset == null && bodyAsString != null) {
				responseCharset = StandardCharsets.UTF_8.name();
			}

			tiTotal.stop();

			Headers responseHeaders = response.headers();
			SetMultimap<String, String> headers = HashMultimap.create(responseHeaders.size(), 2);
			responseHeaders.names().forEach(name -> headers.putAll(name, responseHeaders.values(name)));

			return new ResponseInfo.Builder()
					.methodType(method)
					.uri(uri.toString())
					.uriAlias(template.getUriAlias())
					.statusCode(statusCode)
					.statusMsg(statusMsg)
					.headers(headers)
					.body(bodyBytes)
					.bodyAsString(bodyAsString)
					.charset(responseCharset)
					.contentType(contentTypeString)
					.timestamp(timestamp)
					.timeIntervalBeforeBody(tiBeforeBody)
					.timeIntervalTotal(tiTotal)
					.executionId(executionIdProvider.get())
					.requestId(requestId)
					.build();
		}
	}

	/**
	 * Prepares the request.
	 *
	 * @param uri
	 *            the uri
	 * @param method
	 *            the HTTP method
	 * @param template
	 *            the request template
	 * @param requestId
	 *            the requestId
	 * @return the request object
	 * @throws MalformedURLException
	 *             If a protocol handler for the URL could not be found, or if some other error
	 *             occurred while constructing the URL
	 * @throws URISyntaxException
	 *             If the given string violates RFC 2396, as augmented by the above deviations
	 */
	protected Request prepareRequest(URI uri, final String method, final RequestTemplate template, final UUID requestId)
			throws MalformedURLException, URISyntaxException {
		SetMultimap<String, String> parameters = template.getRequestParameters();
		RequestBody requestBody = null;

		switch (HttpMethod.valueOf(method)) {
			case GET:
			case DELETE:
			case OPTIONS:
			case HEAD:
			case TRACE:
				if (!parameters.isEmpty()) {
					String query = createQueryStringFromParams(parameters);
					uri = new URI(uri.getRawQuery() == null ? uri.toString() + '?' + query : uri.toString() + '&' + query);
				}
				break;
			case POST:
			case PUT:
				Body body = template.getBody();
				if (body != null) {
					requestBody = RequestBody.create(null, body.getContent());
				} else {
					FormEncodingBuilder feb = new FormEncodingBuilder();
					parameters.entries().forEach(entry -> feb.add(entry.getKey(), entry.getValue()));
					requestBody = feb.build();
				}
				break;
			default:
				throw new IllegalStateException("Unknown HTTP method: " + method);
		}

		Request.Builder requestBuilder = requestBuilderProvider.get()
				.url(uri.toURL())
				.addHeader(WebConstants.EXECUTION_ID_HEADER, executionIdProvider.get().toString())
				.addHeader(WebConstants.OPERATION_HEADER, operationProvider.get())
				.addHeader(WebConstants.REQUEST_ID_HEADER, requestId.toString());

		template.getRequestHeaders().entries().forEach(entry -> requestBuilder.addHeader(entry.getKey(), entry.getValue()));
		return requestBuilder.method(method, requestBody).build();
	}

	private String bodyAsString(final byte[] body, final String contentCharset) throws UnsupportedEncodingException {
		if (body != null && contentCharset != null) {
			return new String(body, contentCharset);
		}
		return null;
	}

	private String createQueryStringFromParams(final SetMultimap<String, String> parameters) {
		StrBuilder sb = new StrBuilder();
		parameters.entries().forEach(entry -> {
			sb.appendSeparator('&');
			try {
				sb.append(URLEncoder.encode(entry.getKey(), UTF_8.name()));
				sb.append('=');
				sb.append(URLEncoder.encode(entry.getValue(), UTF_8.name()));
			} catch (UnsupportedEncodingException ex) {
				Throwables.propagate(ex);
			}
		});
		return sb.toString();
	}
}
