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
package com.mgmtp.perfload.core.common.util;

import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Pojo for sending status information on the currently running test run to the console.
 *
 * @author rnaegele
 */
public final class StatusInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private final StatusInfoType type;
	private final Integer processId;
	private final Integer daemonId;
	private final Integer threadId;
	private final String operation;
	private final String target;
	private final Boolean error;
	private final Integer activeThreads;
	private final String stackTrace;
	private final Boolean finished;

	private StatusInfo(final StatusInfoType type, final Integer processId, final Integer daemonId, final Integer threadId,
		final String operation, final String target, final Boolean error, final Integer activeThreads, final String stackTrace,
		final Boolean finished) {
	this.type = type;
	this.processId = processId;
	this.daemonId = daemonId;
	this.threadId = threadId;
	this.operation = operation;
	this.target = target;
	this.error = error;
	this.activeThreads = activeThreads;
	this.stackTrace = stackTrace;
	this.finished = finished;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
	return serialVersionUID;
	}

	/**
	 * @return the type
	 */
	public StatusInfoType getType() {
	return type;
	}

	/**
	 * @return the processId
	 */
	public Integer getProcessId() {
	return processId;
	}

	/**
	 * @return the daemonId
	 */
	public Integer getDaemonId() {
	return daemonId;
	}

	/**
	 * @return the threadId
	 */
	public Integer getThreadId() {
	return threadId;
	}

	/**
	 * @return the operation
	 */
	public String getOperation() {
	return operation;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
	return target;
	}

	/**
	 * @return the error
	 */
	public Boolean getError() {
	return error;
	}

	/**
	 * @return the activeThreads
	 */
	public Integer getActiveThreads() {
	return activeThreads;
	}

	/**
	 * @return the stackTrace
	 */
	public String getStackTrace() {
	return stackTrace;
	}

	/**
	 * @return the finished
	 */
	public Boolean getFinished() {
	return finished;
	}

	@Override
	public String toString() {
	return toDefaultString(this);
	}

	/**
	 * Builder for {@link StatusInfo}.
	 *
	 * @author rnaegele
	 */
	public static class Builder {
	private final StatusInfoType type;
	private final Integer processId;
	private final Integer daemonId;

	private Integer threadId;
	private String operation;
	private String target;
	private Boolean error;
	private Integer activeThreads;
	private String stackTrace;
	private Boolean finished;

	public Builder(final StatusInfoType type, final Integer processId, final Integer daemonId) {
		this.type = type;
		this.processId = processId;
		this.daemonId = daemonId;
	}

	public Builder(final StatusInfo statusInfo) {
		this(statusInfo.getType(), statusInfo.getProcessId(), statusInfo.getDaemonId());
		this.threadId = statusInfo.getThreadId();
		this.operation = statusInfo.getOperation();
		this.target = statusInfo.getTarget();
		this.error = statusInfo.getError();
		this.activeThreads = statusInfo.getActiveThreads();
		this.stackTrace = statusInfo.getStackTrace();
		this.finished = statusInfo.getFinished();
	}

	public Builder threadId(final Integer aThreadId) {
		threadId = aThreadId;
		return this;
	}

	public Builder operation(final String anOperation) {
		operation = anOperation;
		return this;
	}

	public Builder target(final String aTarget) {
		target = aTarget;
		return this;
	}

	public Builder error(final Boolean anError) {
		error = anError;
		return this;
	}

	public Builder activeThreads(final Integer anActiveThreads) {
		activeThreads = anActiveThreads;
		return this;
	}

	public Builder stackTrace(final String aStackTrace) {
		this.stackTrace = aStackTrace;
		return this;
	}

	public Builder finished(final Boolean aFinished) {
		this.finished = aFinished;
		return this;
	}

	public Builder throwable(final Throwable aThrowable) {
		if (aThrowable != null) {
		// We cannot serialize the exception itself because the Exception
		// class might not be on the console's classpath.
		StringWriter sw = new StringWriter();
		aThrowable.printStackTrace(new PrintWriter(sw));
		stackTrace = sw.toString();
		}
		return this;
	}

	public StatusInfo build() {
		return new StatusInfo(type, processId, daemonId, threadId, operation, target, error,
			activeThreads, stackTrace, finished);
	}
	}
}
