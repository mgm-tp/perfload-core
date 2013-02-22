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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class DefaultResponseParserTest {

	private static final byte[] VALID_BODY_BYTES = "<html>This response body is valid</html>".getBytes(Charsets.UTF_8);
	private static final byte[] INVALID_BODY_BYTES = "<html>This response body is invalid".getBytes(Charsets.UTF_8);

	private ResponseInfo createResponseInfo(final int statusCode, final byte[] body) {
		return new ResponseInfo("GET", "/foo", statusCode, "", Collections.<String, String>emptyMap(),
				body, "UTF-8", "text/html", System.currentTimeMillis(),
				new TimeInterval(), new TimeInterval(), UUID.randomUUID(), UUID.randomUUID());
	}

	@Test
	public void testWithValidResponse() throws InvalidResponseException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		List<Pattern> patterns = asList(Pattern.compile("does not match"), Pattern.compile("(?is)^((?!</html>).)*$"));
		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				patterns);
		parser.validate(responseInfo);

		parser = new DefaultResponseParser(ImmutableSet.of(200), ImmutableSet.of(400),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidStatusCode1() throws InvalidResponseException {
		ResponseInfo responseInfo = createResponseInfo(400, VALID_BODY_BYTES);

		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), ImmutableSet.of(400),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidStatusCode2() throws InvalidResponseException {
		ResponseInfo responseInfo = createResponseInfo(400, VALID_BODY_BYTES);

		ResponseParser parser = new DefaultResponseParser(ImmutableSet.of(200), Collections.<Integer>emptySet(),
				Collections.<Pattern>emptyList());
		parser.validate(responseInfo);
	}

	@Test(expectedExceptions = InvalidResponseException.class)
	public void testWithInvalidResponseBody() throws InvalidResponseException {
		ResponseInfo responseInfo = createResponseInfo(200, INVALID_BODY_BYTES);

		List<Pattern> patterns = asList(Pattern.compile("does not match"), Pattern.compile("(?is)^((?!</html>).)*$"));
		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				patterns);
		parser.validate(responseInfo);
	}

	@Test
	public void testSuccessfulDetailExtraction() throws PatternNotFoundException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		DetailExtraction extraction1 = new DetailExtraction("foo", "response (body) is", 1, null, false, true);
		DetailExtraction extraction2 = new DetailExtraction("bar", "body (is) (valid)", 2, null, false, true);
		DetailExtraction extraction3 = new DetailExtraction("baz", "bla blubb", 42, "myDefault", false, true);
		List<DetailExtraction> extractions = asList(extraction1, extraction2, extraction3);

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				Collections.<Pattern>emptyList());
		parser.extractDetails(responseInfo, extractions, pc);

		assertEquals(pc.size(), 3);
		assertThat(pc, hasEntry("foo", "body"));
		assertThat(pc, hasEntry("bar", "valid"));
		assertThat(pc, hasEntry("baz", "myDefault"));
	}

	@Test
	public void testSuccessfulIndexedDetailExtraction() throws PatternNotFoundException {
		ResponseInfo responseInfo = createResponseInfo(200, "<html>bla bluub foo0 foo1 foo2 bla blubb</html>".getBytes(Charsets.UTF_8));

		DetailExtraction extraction1 = new DetailExtraction("foo", "(foo\\d)", 1, null, true, true);
		DetailExtraction extraction2 = new DetailExtraction("bar", "(some random pattern)", 1, "myDefault", true, true);
		List<DetailExtraction> extractions = asList(extraction1, extraction2);

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				Collections.<Pattern>emptyList());
		parser.extractDetails(responseInfo, extractions, pc);

		assertEquals(pc.size(), 4);
		assertThat(pc, hasEntry("foo#0", "foo0"));
		assertThat(pc, hasEntry("foo#1", "foo1"));
		assertThat(pc, hasEntry("foo#2", "foo2"));
		assertThat(pc, hasEntry("bar#0", "myDefault"));
	}

	@Test(expectedExceptions = PatternNotFoundException.class)
	public void testUnsuccessfulDetailExtraction() throws PatternNotFoundException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		DetailExtraction extraction = new DetailExtraction("foo", "bla blubb", 42, null, false, true);
		List<DetailExtraction> extractions = asList(extraction);

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		ResponseParser parser = new DefaultResponseParser(Collections.<Integer>emptySet(), Collections.<Integer>emptySet(),
				Collections.<Pattern>emptyList());
		parser.extractDetails(responseInfo, extractions, pc);
	}
}
