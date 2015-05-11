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
package com.mgmtp.perfload.core.client.web.event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mgmtp.perfload.core.client.web.flow.DefaultRequestFlowHandler;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;

/**
 * Represents an event triggered by {@link DefaultRequestFlowHandler} before and after a request
 * flow as well as before and after each request.
 * 
 * @author rnaegele
 */
public final class RequestFlowEvent {

	private final RequestTemplate requestTemplate;
	private final int flowIndex;
	private final Exception exception;
	private final ResponseInfo responseInfo;

	/**
	 * @param flowIndex
	 *            the current request flow index (1-based)
	 */
	public RequestFlowEvent(final int flowIndex) {
		this(flowIndex, (RequestTemplate) null);
	}

	/**
	 * 
	 * @param flowIndex
	 *            the current request flow index (1-based)
	 * @param exception
	 *            an exception that occurred during the execution of the request flow
	 */
	public RequestFlowEvent(final int flowIndex, final Exception exception) {
		this(flowIndex, null, exception, null);
	}

	/**
	 * @param flowIndex
	 *            the current request flow index (1-based)
	 * @param requestTemplate
	 *            the request template
	 */
	public RequestFlowEvent(final int flowIndex, final RequestTemplate requestTemplate) {
		this(flowIndex, requestTemplate, null, null);
	}

	/**
	 * @param flowIndex
	 *            the current request flow index (1-based)
	 * @param requestTemplate
	 *            the request template
	 * @param exception
	 *            an exception that occurred during the execution of the request flow
	 * @param responseInfo
	 *            the response info object
	 */
	public RequestFlowEvent(final int flowIndex, final RequestTemplate requestTemplate,
			final Exception exception, final ResponseInfo responseInfo) {
		this.flowIndex = flowIndex;
		this.requestTemplate = requestTemplate;
		this.exception = exception;
		this.responseInfo = responseInfo;
	}

	/**
	 * @return {@true}, if the exception property is set
	 */
	public boolean isError() {
		return exception != null;
	}

	/**
	 * Returns the request template of the current request.
	 * 
	 * @return the requestTemplate
	 */
	public RequestTemplate getRequestTemplate() {
		return requestTemplate;
	}

	/**
	 * Returns the 1-based index of the current request flow.
	 * 
	 * @return the flowIndex
	 */
	public int getFlowIndex() {
		return flowIndex;
	}

	/**
	 * Returns the error message if the exception property is set. If the exception returns a
	 * {@code null} message, the cause of the exception is checked as well.
	 * 
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		if (exception != null) {
			String msg = exception.getMessage();
			if (msg == null) {
				Throwable cause = exception.getCause();
				if (cause != null) {
					msg = cause.getMessage();
				}
				if (msg == null) {
					msg = "An error occurred. No message was supplied.";
				}
			}
			return msg;
		}
		return null;
	}

	/**
	 * Returns an exception that was thrown during request flow execution.
	 * 
	 * @return the exception, or {@code null}
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Returns a {@link ResponseInfo} object that encapsulates information about the HTTP response.
	 * 
	 * @return the responseInfo
	 */
	public ResponseInfo getResponseInfo() {
		return responseInfo;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		tsb.append("flowIndex", flowIndex);
		tsb.append("exception", exception);
		return tsb.toString();
	}
}
