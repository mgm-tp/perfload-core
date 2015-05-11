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

import static com.google.common.collect.Sets.intersection;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class DefaultResponseValidatorTest {

	private static final byte[] VALID_BODY_BYTES = "<html>This response body is valid</html>".getBytes(Charsets.UTF_8);
	private static final byte[] INVALID_BODY_BYTES = "<html>This response body is invalid".getBytes(Charsets.UTF_8);

	private ResponseInfo createResponseInfo(final int statusCode, final byte[] body) throws UnsupportedEncodingException {
		return new ResponseInfo("GET", "/foo", statusCode, "", Collections.<String, String>emptyMap(),
				body, new String(body, "UTF-8"), "UTF-8", "text/html", System.currentTimeMillis(),
				new TimeInterval(), new TimeInterval(), UUID.randomUUID(), UUID.randomUUID());
	}

	@Test
	public void testWithValidResponse() throws InvalidResponseException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		List<Pattern> patterns = asList(Pattern.compile("does not match"), Pattern.compile("(?is)^((?!</html>).)*$"));
		ResponseValidator parser = new DefaultResponseValidator(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				patterns);
		parser.validate(responseInfo);

		parser = new DefaultResponseValidator(ImmutableSet.of(200), ImmutableSet.of(400),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidStatusCode1() throws InvalidResponseException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(400, VALID_BODY_BYTES);

		ResponseValidator parser = new DefaultResponseValidator(Collections.<Integer>emptySet(), ImmutableSet.of(400),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidStatusCode2() throws InvalidResponseException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(400, VALID_BODY_BYTES);

		ResponseValidator parser = new DefaultResponseValidator(ImmutableSet.of(200), Collections.<Integer>emptySet(),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidResponseBody() throws InvalidResponseException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(200, INVALID_BODY_BYTES);

		List<Pattern> patterns = asList(Pattern.compile("does not match"), Pattern.compile("(?is)^((?!</html>).)*$"));
		ResponseValidator parser = new DefaultResponseValidator(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				patterns);
		parser.validate(responseInfo);
	}

	@Test
	public void testWithSameAllowedAndForbiddenStatusCode() throws InvalidResponseException {
		ImmutableSet<Integer> statusCodes = ImmutableSet.of(200);
		Set<Integer> intersection = intersection(statusCodes, statusCodes);
		try {
			DefaultResponseValidator validator = new DefaultResponseValidator(statusCodes, statusCodes,
					asList(Pattern.compile("")));
			validator.validate(null);
			fail("Expected IllegalStateException to be thrown.");
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage()).endsWith(intersection.toString());
		}
	}
}
