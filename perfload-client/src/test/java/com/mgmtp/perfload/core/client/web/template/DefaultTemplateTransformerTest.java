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
package com.mgmtp.perfload.core.client.web.template;

import static com.google.common.io.Resources.getResource;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;

/**
 * @author rnaegele
 */
public class DefaultTemplateTransformerTest {

	@Test
	public void testTransformation() throws IOException {
		SetMultimap<String, String> params = HashMultimap.<String, String>create();
		params.put("param1", "${value1}");
		params.put("${param2}", "value2");
		params.put("${param3}", "${value3}");
		params.put("${param4}", "${value4}");
		params.put("param5", "value5");
		params.put("indexedParam", "value0");
		params.put("indexedParam", "${indexedValue}");
		params.put("${indexedParam}", "value2");

		SetMultimap<String, String> headers = HashMultimap.<String, String>create();
		headers.put("header1", "header1value");

		HeaderExtraction heActual = new HeaderExtraction("header1", "blubb");
		DetailExtraction deActual = new DetailExtraction("foo", "${foo}", "1", null, "false", "false");
		RequestTemplate template = new RequestTemplate("${type}", "${skip}", "${uri}", "${uriAlias}", headers, params,
				Body.create("blubb ${foo} blubb ${foo} blubb"),
				ImmutableList.<HeaderExtraction>of(heActual), ImmutableList.<DetailExtraction>of(deActual), "${skip}");

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		pc.put("type", "GET");
		pc.put("value1", "value1_transformed");
		pc.put("param2", "param2_transformed");
		pc.put("param3", "param3_transformed");
		pc.put("value3", "value3_transformed");
		pc.put("indexedValue", "value1");
		pc.put("indexedParam", "indexedParam");
		pc.put("foo", ".*?");
		pc.put("skip", "false");
		pc.put("uri", "http://localhost");
		pc.put("uriAlias", "myUriAlias");

		TemplateTransformer transformer = new DefaultTemplateTransformer();
		RequestTemplate executableTemplate = transformer.makeExecutable(template, pc);

		assertEquals(executableTemplate.getType(), "GET");
		assertEquals(executableTemplate.getSkip(), "false");
		assertFalse(executableTemplate.isSkipped());
		assertEquals(executableTemplate.getUri(), "http://localhost");
		assertEquals(executableTemplate.getUriAlias(), "myUriAlias");
		assertFalse(executableTemplate.isValidateResponse());
		assertEquals(executableTemplate.getBody().getContent(), "blubb .*? blubb .*? blubb".getBytes(Charsets.UTF_8.name()));

		HeaderExtraction heExpected = new HeaderExtraction("header1", "blubb");
		assertEquals(executableTemplate.getHeaderExtractions().get(0), heExpected);

		DetailExtraction deExpected = new DetailExtraction("foo", ".*?", "1", null, "false", "false");
		assertEquals(executableTemplate.getDetailExtractions().get(0).getPattern(), deExpected.getPattern());

		SetMultimap<String, String> expextedHeaders = executableTemplate.getRequestHeaders();
		assertEquals(expextedHeaders.size(), template.getRequestHeaders().size());
		assertTrue(expextedHeaders.containsEntry("header1", "header1value"));

		SetMultimap<String, String> expextedParams = executableTemplate.getRequestParameters();
		assertEquals(expextedParams.size(), template.getRequestParameters().size());

		assertTrue(expextedParams.containsEntry("param1", "value1_transformed"));
		assertTrue(expextedParams.containsEntry("param2_transformed", "value2"));
		assertTrue(expextedParams.containsEntry("param3_transformed", "value3_transformed"));
		assertTrue(expextedParams.containsEntry("${param4}", "${value4}"));
		assertTrue(expextedParams.containsEntry("param5", "value5"));
		assertTrue(expextedParams.containsEntry("indexedParam", "value0"));
		assertTrue(expextedParams.containsEntry("indexedParam", "value1"));
		assertTrue(expextedParams.containsEntry("indexedParam", "value2"));

		ImmutableSetMultimap.<String, String>of();
		template = new RequestTemplate("GET", "true", "testuri/param1/${value1}/param2/${value2}", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "false");
		executableTemplate = transformer.makeExecutable(template, pc);

		assertEquals(executableTemplate.getUri(), "testuri/param1/value1_transformed/param2/${value2}");
	}

	@Test
	public void testTransformationWithBinaryBodyResource() throws IOException {
		RequestTemplate template = new RequestTemplate("GET", "false", "uri", "alias", ImmutableSetMultimap.<String, String>of(),
				ImmutableSetMultimap.<String, String>of(), Body.create("${resource}", ResourceType.binary.name()),
				ImmutableList.<HeaderExtraction>of(), ImmutableList.<DetailExtraction>of(), "true");

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		pc.put("resource", "fooResource");

		TemplateTransformer transformer = new DefaultTemplateTransformer();
		RequestTemplate executableTemplate = transformer.makeExecutable(template, pc);

		byte[] expectedResource = toByteArray(getResource("fooResource"));
		assertEquals(executableTemplate.getBody().getContent(), expectedResource);
	}

	@Test
	public void testTransformationWithTextBodyResource() throws IOException {
		RequestTemplate template = new RequestTemplate("GET", "false", "uri", "alias", ImmutableSetMultimap.<String, String>of(),
				ImmutableSetMultimap.<String, String>of(), Body.create("${resource}", "${resourceType}"),
				ImmutableList.<HeaderExtraction>of(), ImmutableList.<DetailExtraction>of(), "true");

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		pc.put("resource", "fooResource");
		pc.put("resourceType", ResourceType.text.name());
		pc.put("test", "testvalue");

		TemplateTransformer transformer = new DefaultTemplateTransformer();
		RequestTemplate executableTemplate = transformer.makeExecutable(template, pc);

		assertEquals(new String(executableTemplate.getBody().getContent(), Charsets.UTF_8), "test testvalue");
	}
}
