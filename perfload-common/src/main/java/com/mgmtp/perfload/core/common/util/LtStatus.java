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

/**
 * Enum for a test's execution status.
 * 
 * @author rnaegele
 */
public enum LtStatus {
	/** Status for a successful test */
	SUCCESSFUL("Test finished successfully."),

	/** Status for a test that was aborted due to an error */
	ERROR("Test terminated with an error."),

	/** Status for a test that was aborted due to an interrupt */
	INTERRUPTED("Test aborted due to an interrupt.");

	private final String msg;

	private LtStatus(final String msg) {
		this.msg = msg;
	}

	/**
	 * @return the status message
	 */
	public String getMsg() {
		return msg;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
