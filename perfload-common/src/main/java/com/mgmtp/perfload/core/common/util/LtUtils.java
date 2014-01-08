/*
 * Copyright (c) 2002-2014 mgm technology partners GmbH
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rnaegele
 */
public final class LtUtils {

	private static final Logger LOG = LoggerFactory.getLogger(LtUtils.class);

	private LtUtils() {
		//
	}

	/**
	 * Throws an {@link AbortionException} if the current thread has been interrupted.
	 */
	public static void checkInterrupt() {
		if (Thread.currentThread().isInterrupted()) {
			LOG.debug("Interrupt detected!");
			throw new AbortionException(LtStatus.INTERRUPTED, "Aborting test due to interrupt.");
		}
	}

	/**
	 * Creates a string representation of the specified object using {@link ToStringBuilder} with
	 * {@link ToStringStyle#SHORT_PREFIX_STYLE}.
	 * 
	 * @param object
	 *            the object
	 * @return the string representation of the specified object
	 */
	public static String toDefaultString(final Object object) {
		return ToStringBuilder.reflectionToString(object, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
