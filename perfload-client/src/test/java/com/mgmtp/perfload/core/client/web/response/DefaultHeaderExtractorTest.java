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

import java.util.UUID;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSetMultimap;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class DefaultHeaderExtractorTest {
	private ResponseInfo createResponseInfo() {
		return new ResponseInfo.Builder()
				.methodType("GET")
				.uri("/foo")
				.statusCode(200)
				.statusMsg("")
				.headers(ImmutableSetMultimap.of("header1", "value1"))
				.charset("UTF-8")
				.contentType("text/html")
				.timestamp(System.currentTimeMillis())
				.timeIntervalBeforeBody(new TimeInterval())
				.timeIntervalTotal(new TimeInterval())
				.executionId(UUID.randomUUID())
				.requestId(UUID.randomUUID())
				.build();
	}

	@Test
	public void testHeaderExtractionWithDefaultPlaceholderName() {
		HeaderExtraction headerExtraction = new HeaderExtraction("header1", null);
		PlaceholderContainer pc = performExtraction(createResponseInfo(), headerExtraction);
		assertEquals(pc.size(), 1);
		assertThat(pc).contains(entry("header1", "value1"));
	}

	@Test
	public void testHeaderExtractionWithAlternativePlaceholderName() {
		HeaderExtraction headerExtraction = new HeaderExtraction("header1", "myAlternativeName");
		PlaceholderContainer pc = performExtraction(createResponseInfo(), headerExtraction);
		assertEquals(pc.size(), 1);
		assertThat(pc).contains(entry("myAlternativeName", "value1"));
	}

	private PlaceholderContainer performExtraction(final ResponseInfo responseInfo, final HeaderExtraction extraction) {
		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		HeaderExtractor extractor = new DefaultHeaderExtractor();
		extractor.extractHeaders(responseInfo, asList(extraction), pc);
		return pc;
	}
}
