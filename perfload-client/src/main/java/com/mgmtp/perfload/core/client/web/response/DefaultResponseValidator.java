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
package com.mgmtp.perfload.core.client.web.response;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.intersection;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mgmtp.perfload.core.client.web.config.annotations.AllowedStatusCodes;
import com.mgmtp.perfload.core.client.web.config.annotations.ErrorPatterns;
import com.mgmtp.perfload.core.client.web.config.annotations.ForbiddenStatusCodes;

/**
 * Default response validator implementation.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public class DefaultResponseValidator implements ResponseValidator {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final List<Pattern> errorPatterns;
	private final Set<Integer> allowedStatusCodes;
	private final Set<Integer> forbiddenStatusCodes;

	/**
	 * @param allowedStatusCodes
	 *            status codes that constitute a valid response; if empty, all status codes are
	 *            valid
	 * @param forbiddenStatusCodes
	 *            status codes that constitutes an invalid response
	 * @param errorPatterns
	 *            A list of regular expression patterns used for parsing the response. A response is
	 *            considered invalid if any of these patterns is found in the response using
	 *            {@link Matcher#find()}.
	 */
	@Inject
	public DefaultResponseValidator(@AllowedStatusCodes final Set<Integer> allowedStatusCodes,
			@ForbiddenStatusCodes final Set<Integer> forbiddenStatusCodes, @ErrorPatterns final List<Pattern> errorPatterns) {
		checkArgument(allowedStatusCodes != null, "Parameter 'allowdStatusCodes' may not be null.");
		checkArgument(forbiddenStatusCodes != null, "Parameter 'forbiddenStatusCodes' may not be null.");
		checkArgument(errorPatterns != null, "Parameter 'errorPatterns' may not be null.");

		Set<Integer> intersection = intersection(allowedStatusCodes, forbiddenStatusCodes);
		checkState(intersection.isEmpty(),
				"Allowed and forbidden status codes must be mutually exclusive but have the following intersection: %s",
				intersection);

		this.allowedStatusCodes = ImmutableSet.copyOf(allowedStatusCodes);
		this.forbiddenStatusCodes = ImmutableSet.copyOf(forbiddenStatusCodes);
		this.errorPatterns = ImmutableList.copyOf(errorPatterns);
	}

	/**
	 * Validates the response checking for forbidden status codes, valid status codes, and parsing
	 * for errors in the response body, in that order.
	 * 
	 * @throws InvalidResponseException
	 *             if the response is not valid
	 */
	@Override
	public void validate(final ResponseInfo responseInfo) throws InvalidResponseException {
		log.debug("Validating response...");

		int statusCode = responseInfo.getStatusCode();
		boolean success = !forbiddenStatusCodes.contains(statusCode);
		if (success && !allowedStatusCodes.isEmpty() && !allowedStatusCodes.contains(statusCode)) {
			success = false;
		}
		if (!success) {
			throw new InvalidResponseException("Response code not allowed: " + statusCode);
		}

		String body = responseInfo.getBodyAsString();
		for (Pattern pattern : errorPatterns) {
			if (body != null) {
				Matcher matcher = pattern.matcher(body);
				if (matcher.find()) {
					throw new InvalidResponseException("Error pattern matched: " + pattern);
				}
			} else {
				log.warn("Response body is empty or not recognized as text. Skip checking for error pattern: {}", pattern);
			}
		}
	}
}
