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
package com.mgmtp.perfload.core.web.response;

import static java.util.Arrays.copyOf;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.ImmutableMap;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * Pojo for wrapping response information.
 * 
 * @author rnaegele
 */
public final class ResponseInfo {
	private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\r?\n");

	private final int statusCode;
	private final String statusMsg;
	private final byte[] body;
	private final Map<String, String> headers;
	private TimeInterval timeIntervalTotal;
	private TimeInterval timeIntervalBeforeBody;
	private final String uri;
	private String uriAlias;
	private final String methodType;
	private final String charset;
	private final String contentType;
	private String bodyAsString;
	private Object extraInfo;
	private long timestamp;
	private final UUID executionId;
	private final UUID requestId;

	/**
	 * @param methodType
	 *            the method type (e. g. GET, POST)
	 * @param uri
	 *            the URI of the request
	 * @param statusCode
	 *            the status code
	 * @param statusMsg
	 *            the status message
	 * @param headers
	 *            a map of response headers
	 * @param body
	 *            the response body
	 * @param charset
	 *            the response character set
	 * @param contentType
	 *            the response content type
	 * @param timestamp
	 *            a timestamp taken before the request is executed
	 * @param timeIntervalBeforeBody
	 *            the time interval representing the execution time of the request without the
	 *            retrieval of the request body
	 * @param timeIntervalTotal
	 *            the time interval representing the execution time of the request including the
	 *            retrieval of the request body
	 * @param executionId
	 *            the id of the current execution
	 * @param requestId
	 *            the id of the current request
	 */
	public ResponseInfo(final String methodType, final String uri, final int statusCode, final String statusMsg,
			final Map<String, String> headers, final byte[] body, final String charset, final String contentType,
			final long timestamp, final TimeInterval timeIntervalBeforeBody, final TimeInterval timeIntervalTotal,
			final UUID executionId, final UUID requestId) {
		this.methodType = methodType;
		this.uri = uri;
		this.statusCode = statusCode;
		this.statusMsg = statusMsg;
		this.headers = ImmutableMap.copyOf(headers);
		this.body = body != null ? copyOf(body, body.length) : null;
		this.charset = charset;
		this.contentType = contentType;
		this.timestamp = timestamp;
		this.timeIntervalBeforeBody = timeIntervalBeforeBody;
		this.timeIntervalTotal = timeIntervalTotal;
		this.executionId = executionId;
		this.requestId = requestId;
	}

	/**
	 * @param methodType
	 *            the method type (e. g. GET, POST)
	 * @param uri
	 *            the URI of the request
	 * @param timestamp
	 *            a timestamp taken before the request is executed
	 * @param executionId
	 *            the id of the current execution
	 * @param requestId
	 *            the id of the current request
	 */
	public ResponseInfo(final String methodType, final String uri, final long timestamp, final UUID executionId, final UUID requestId) {
		this(methodType, uri, -1, null, ImmutableMap.<String, String>of(), null, null, null, timestamp, new TimeInterval(),
				new TimeInterval(), executionId, requestId);
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @return the statusMsg
	 */
	public String getStatusMsg() {
		return statusMsg;
	}

	/**
	 * @return the body
	 */
	public byte[] getBody() {
		return copyOf(body, body.length);
	}

	/**
	 * Converts and caches the response body to a string using the response character set if the
	 * body is non-{@code null} and of type text (i. e. its content type starts with "text"). If the
	 * character set is {@code null}, the platform default is used.
	 * 
	 * @return the response body as string, or {@code null} if the body is empty or not of type text
	 */
	public String getResponseBodyAsString() {
		if (bodyAsString == null && body != null && startsWith(contentType, "text")) {
			try {
				bodyAsString = charset != null ? new String(body, charset) : new String(body);
			} catch (UnsupportedEncodingException ex) {
				// Cannot normally happen
				throw new IllegalStateException(ex);
			}
		}
		return bodyAsString;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the timeIntervalTotal
	 */
	public TimeInterval getTimeIntervalTotal() {
		return timeIntervalTotal;
	}

	public void setTimeIntervalTotal(final TimeInterval timeIntervalTotal) {
		this.timeIntervalTotal = timeIntervalTotal;
	}

	/**
	 * @return the timeIntervalBeforeBody
	 */
	public TimeInterval getTimeIntervalBeforeBody() {
		return timeIntervalBeforeBody;
	}

	public void setTimeIntervalBeforeBody(final TimeInterval timeIntervalBeforeBody) {
		this.timeIntervalBeforeBody = timeIntervalBeforeBody;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the uriAlias
	 */
	public String getUriAlias() {
		return uriAlias;
	}

	/**
	 * @param uriAlias
	 *            an alias for the URI used for logging measurings
	 */
	public void setUriAlias(final String uriAlias) {
		this.uriAlias = uriAlias;
	}

	/**
	 * @return the methodType
	 */
	public String getMethodType() {
		return methodType;
	}

	/**
	 * @return the executionId
	 */
	public UUID getExecutionId() {
		return executionId;
	}

	/**
	 * @return the requestId
	 */
	public UUID getRequestId() {
		return requestId;
	}

	/**
	 * @return the extraInfo
	 */
	public Object getExtraInfo() {
		return extraInfo;
	}

	/**
	 * @param extraInfo
	 *            the extraInfo
	 */
	public void setExtraInfo(final Object extraInfo) {
		this.extraInfo = extraInfo;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		tsb.append("uri", uri);
		tsb.append("methodType", methodType);
		tsb.append("statusCode", statusCode);
		tsb.append("timeIntervalBeforeBody", timeIntervalBeforeBody);
		tsb.append("timeIntervalTotal", timeIntervalTotal);
		tsb.append("headers", headers);
		tsb.append("charset", charset);
		tsb.append("extraInfo", extraInfo);
		tsb.append("executionId", executionId);
		tsb.append("body", getResponseBodyAsString());

		// We don't want line breaks in the result
		return LINE_BREAK_PATTERN.matcher(tsb.toString()).replaceAll(" ");
	}
}
