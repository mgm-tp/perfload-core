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
package com.mgmtp.perfload.core.client.web.response;

import static java.util.Arrays.copyOf;

import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
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
	private final SetMultimap<String, String> headers;
	private final TimeInterval timeIntervalTotal;
	private final TimeInterval timeIntervalBeforeBody;
	private final String uri;
	private final String uriAlias;
	private final String methodType;
	private final String charset;
	private final String contentType;
	private final String bodyAsString;
	private Object extraInfo;
	private final long timestamp;
	private final UUID executionId;
	private final UUID requestId;

	private ResponseInfo(final Builder builder) {
		this.methodType = builder.methodType;
		this.uri = builder.uri;
		this.uriAlias = builder.uriAlias;
		this.statusCode = builder.statusCode;
		this.statusMsg = builder.statusMsg;
		this.headers = builder.headers != null ? ImmutableSetMultimap.copyOf(builder.headers) : ImmutableSetMultimap.of();
		this.body = builder.body;
		this.bodyAsString = builder.bodyAsString;
		this.charset = builder.charset;
		this.contentType = builder.contentType;
		this.timestamp = builder.timestamp;
		this.timeIntervalBeforeBody = builder.timeIntervalBeforeBody != null ? builder.timeIntervalBeforeBody : new TimeInterval();
		this.timeIntervalTotal = builder.timeIntervalTotal != null ? builder.timeIntervalTotal : new TimeInterval();
		this.executionId = builder.executionId;
		this.requestId = builder.requestId;
		this.extraInfo = builder.extraInfo;
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
		if (body == null) {
			return null;
		}
		return copyOf(body, body.length);
	}

	/**
	 * Converts and caches the response body to a string using the response character set if the
	 * body is non-{@code null} and of type text (i. e. its content type starts with "text"). If the
	 * character set is {@code null}, the platform default is used.
	 *
	 * @return the response body as string, or {@code null} if the body is empty or not of type text
	 */
	public String getBodyAsString() {
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
	public SetMultimap<String, String> getHeaders() {
		return headers;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the timeIntervalTotal
	 */
	public TimeInterval getTimeIntervalTotal() {
		return timeIntervalTotal;
	}

	/**
	 * @return the timeIntervalBeforeBody
	 */
	public TimeInterval getTimeIntervalBeforeBody() {
		return timeIntervalBeforeBody;
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
		if (bodyAsString != null) {
			tsb.append("body", bodyAsString);
		}
		// We don't want line breaks in the result
		return LINE_BREAK_PATTERN.matcher(tsb.toString()).replaceAll(" ");
	}

	public static class Builder {
		private int statusCode;
		private String statusMsg;
		private byte[] body;
		private SetMultimap<String, String> headers;
		private TimeInterval timeIntervalTotal;
		private TimeInterval timeIntervalBeforeBody;
		private String uri;
		private String uriAlias;
		private String methodType;
		private String charset;
		private String contentType;
		private String bodyAsString;
		private Object extraInfo;
		private long timestamp;
		private UUID executionId;
		private UUID requestId;

		public Builder() {
			//
		}

		public Builder(final ResponseInfo responseInfo) {
			this.statusCode = responseInfo.statusCode;
			this.statusMsg = responseInfo.statusMsg;
			this.body = responseInfo.body;
			this.headers = responseInfo.headers;
			this.timeIntervalTotal = responseInfo.timeIntervalTotal;
			this.timeIntervalBeforeBody = responseInfo.timeIntervalBeforeBody;
			this.uri = responseInfo.uri;
			this.uriAlias = responseInfo.uriAlias;
			this.methodType = responseInfo.methodType;
			this.charset = responseInfo.charset;
			this.contentType = responseInfo.contentType;
			this.bodyAsString = responseInfo.bodyAsString;
			this.extraInfo = responseInfo.extraInfo;
			this.timestamp = responseInfo.timestamp;
			this.executionId = responseInfo.executionId;
			this.requestId = responseInfo.requestId;
		}

		public Builder statusCode(final int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder statusMsg(final String statusMsg) {
			this.statusMsg = statusMsg;
			return this;
		}

		public Builder body(final byte[] body) {
			this.body = body;
			return this;
		}

		public Builder headers(final SetMultimap<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public Builder timeIntervalTotal(final TimeInterval timeIntervalTotal) {
			this.timeIntervalTotal = timeIntervalTotal;
			return this;
		}

		public Builder timeIntervalBeforeBody(final TimeInterval timeIntervalBeforeBody) {
			this.timeIntervalBeforeBody = timeIntervalBeforeBody;
			return this;
		}

		public Builder uri(final String uri) {
			this.uri = uri;
			return this;
		}

		public Builder uriAlias(final String uriAlias) {
			this.uriAlias = uriAlias;
			return this;
		}

		public Builder methodType(final String methodType) {
			this.methodType = methodType;
			return this;
		}

		public Builder charset(final String charset) {
			this.charset = charset;
			return this;
		}

		public Builder contentType(final String contentType) {
			this.contentType = contentType;
			return this;
		}

		public Builder bodyAsString(final String bodyAsString) {
			this.bodyAsString = bodyAsString;
			return this;
		}

		public Builder extraInfo(final Object extraInfo) {
			this.extraInfo = extraInfo;
			return this;
		}

		public Builder timestamp(final long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder executionId(final UUID executionId) {
			this.executionId = executionId;
			return this;
		}

		public Builder requestId(final UUID requestId) {
			this.requestId = requestId;
			return this;
		}

		public ResponseInfo build() {
			return new ResponseInfo(this);
		}
	}
}
