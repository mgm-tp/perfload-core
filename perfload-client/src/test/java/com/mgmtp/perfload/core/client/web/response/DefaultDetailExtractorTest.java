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

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;
import static org.testng.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class DefaultDetailExtractorTest {
	private static final byte[] VALID_BODY_BYTES = "<html>This response body is valid</html>".getBytes(Charsets.UTF_8);

	private ResponseInfo createResponseInfo(final int statusCode, final byte[] body) throws UnsupportedEncodingException {
		return new ResponseInfo("GET", "/foo", statusCode, "", Collections.<String, String>emptyMap(),
				body, new String(body, "UTF-8"), "UTF-8", "text/html", System.currentTimeMillis(),
				new TimeInterval(), new TimeInterval(), UUID.randomUUID(), UUID.randomUUID());
	}

	@Test
	public void testSuccessfulDetailExtraction() throws PatternNotFoundException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		DetailExtraction extraction1 = new DetailExtraction("foo", "response (body) is", "1", null, "false", "true");
		DetailExtraction extraction2 = new DetailExtraction("bar", "body (is) (valid)", "2", null, "false", "true");
		DetailExtraction extraction3 = new DetailExtraction("baz", "bla blubb", "42", "myDefault", "false", "true");

		PlaceholderContainer pc = performExtraction(responseInfo, extraction1, extraction2, extraction3);

		assertEquals(pc.size(), 3);
		assertThat(pc).contains(entry("foo", "body"));
		assertThat(pc).contains(entry("bar", "valid"));
		assertThat(pc).contains(entry("baz", "myDefault"));
	}

	@Test
	public void testSuccessfulIndexedDetailExtraction() throws PatternNotFoundException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(200,
				"<html>bla bluub foo0 foo1 foo2 bla blubb</html>".getBytes(Charsets.UTF_8));

		DetailExtraction extraction1 = new DetailExtraction("foo", "(foo\\d)", "1", null, "true", "true");
		DetailExtraction extraction2 = new DetailExtraction("bar", "(some random pattern)", "1", "myDefault", "true", "true");

		PlaceholderContainer pc = performExtraction(responseInfo, extraction1, extraction2);

		assertEquals(pc.size(), 4);
		assertThat(pc).contains(entry("foo#0", "foo0"));
		assertThat(pc).contains(entry("foo#1", "foo1"));
		assertThat(pc).contains(entry("foo#2", "foo2"));
		assertThat(pc).contains(entry("bar#0", "myDefault"));
	}

	@Test(expectedExceptions = PatternNotFoundException.class)
	public void testUnsuccessfulDetailExtraction() throws PatternNotFoundException, UnsupportedEncodingException {
		ResponseInfo responseInfo = createResponseInfo(200, VALID_BODY_BYTES);

		DetailExtraction extraction = new DetailExtraction("foo", "bla blubb", "42", null, "false", "true");
		performExtraction(responseInfo, extraction);
	}

	private PlaceholderContainer performExtraction(final ResponseInfo responseInfo, final DetailExtraction... extractions)
			throws PatternNotFoundException {
		List<DetailExtraction> extractionsList = asList(extractions);
		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		DetailExtractor extractor = new DefaultDetailExtractor();
		extractor.extractDetails(responseInfo, extractionsList, pc);
		return pc;
	}
}
