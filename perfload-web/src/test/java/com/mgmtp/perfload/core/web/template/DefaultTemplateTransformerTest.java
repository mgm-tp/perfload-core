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
package com.mgmtp.perfload.core.web.template;

import static org.testng.Assert.assertEquals;

import java.nio.charset.Charset;

import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.web.template.RequestTemplate.HeaderExtraction;

/**
 * @author rnaegele
 */
public class DefaultTemplateTransformerTest {

	@Test
	public void testTransformer() {
		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		pc.put("foo", "foovalue");
		pc.put("bar", "barkey");
		pc.put("uri", "http://localhost");
		pc.put("uriAlias", "myUriAlias");
		pc.put("pattern", ".*");

		SetMultimap<String, String> paramsHeadersMultiMap = HashMultimap.create();
		paramsHeadersMultiMap.put("foo", "${foo}");
		paramsHeadersMultiMap.put("${bar}", "bar");

		Charset charset = Charset.forName("UTF-8");
		RequestTemplate actual = new RequestTemplate("foo", "${uri}", "${uriAlias}", paramsHeadersMultiMap, paramsHeadersMultiMap,
				new Body("${foo}".getBytes(charset), charset), ImmutableList.<HeaderExtraction>of(),
				ImmutableList.of(new DetailExtraction("foo", "${pattern}", 1, null, false, false)));

		paramsHeadersMultiMap = HashMultimap.create();
		paramsHeadersMultiMap.put("foo", "foovalue");
		paramsHeadersMultiMap.put("barkey", "bar");

		TemplateTransformer tt = new DefaultTemplateTransformer();
		RequestTemplate transformed = tt.makeExecutable(actual, pc);

		assertEquals(transformed.getType(), "foo");
		assertEquals(transformed.getUri(), "http://localhost");
		assertEquals(transformed.getUriAlias(), "myUriAlias");
		assertEquals(transformed.getRequestHeaders(), paramsHeadersMultiMap);
		assertEquals(transformed.getRequestParameters(), paramsHeadersMultiMap);
		assertEquals(transformed.getDetailExtractions(), ImmutableList.of(new DetailExtraction("foo", ".*", 1, null, false, false)));
		assertEquals(transformed.getBody(), new Body("foovalue".getBytes(charset), charset));
	}
}
