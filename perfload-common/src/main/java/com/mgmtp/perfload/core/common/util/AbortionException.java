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

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This exception must be thrown in cases that should lead to an abortion of the complete load test.
 * 
 * @author rnaegele
 */
public final class AbortionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final LtStatus status;

	/**
	 * Constructs a new {@link AbortionException} with the specified status, detail message and
	 * cause.
	 * 
	 * @param status
	 *            the status (allowed values are {@link LtStatus#ERROR} and
	 *            {@link LtStatus#INTERRUPTED})
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * 
	 * @param cause
	 *            cause the cause (which is saved for later retrieval by the {@link #getCause()}
	 *            method). (A <tt>null</tt> value is permitted, and indicates that the cause is
	 *            nonexistent or unknown.)
	 */
	public AbortionException(final LtStatus status, final String message, final Throwable cause) {
		super(message, cause);
		checkArgument(status != LtStatus.SUCCESSFUL, "Exception status cannot be " + LtStatus.SUCCESSFUL);
		this.status = status;
	}

	/**
	 * Constructs a new {@link AbortionException} with the specified status.
	 * 
	 * @param status
	 *            the status (allowed values are {@link LtStatus#ERROR} and
	 *            {@link LtStatus#INTERRUPTED})
	 */
	public AbortionException(final LtStatus status) {
		this(status, null);
	}

	/**
	 * Constructs a new {@link AbortionException} with the specified status and detail message.
	 * 
	 * @param status
	 *            the status (allowed values are {@link LtStatus#ERROR} and
	 *            {@link LtStatus#INTERRUPTED})
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 */
	public AbortionException(final LtStatus status, final String message) {
		this(status, message, null);
	}

	/**
	 * @return the status
	 */
	public LtStatus getStatus() {
		return status;
	}
}
