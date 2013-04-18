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
package com.mgmtp.perfload.core.client.web.io;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.mgmtp.perfload.core.common.util.RegexMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.fail;

import java.nio.charset.Charset;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.io.XmlRequestFlowReader;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;

/**
 * @author rnaegele
 */
public class XmlRequestFlowReaderTest {

	@Test
	public void testReader() throws Exception {
		XmlRequestFlowReader reader = new XmlRequestFlowReader("", "request-flow.xml", "UTF-8");
		RequestFlow flow = reader.readFlow();

		// first template
		RequestTemplate template = get(flow, 0);
		assertThat(template.getType(), is(equalTo("GET")));
		assertThat(template.getUri(), is(equalTo("/foo/")));
		assertThat(template.getBody(), is(nullValue()));
		assertThat(template.getDetailExtractions().isEmpty(), is(true));
		assertThat(template.getHeaderExtractions().isEmpty(), is(true));
		assertThat(template.getRequestHeaders().isEmpty(), is(true));

		SetMultimap<String, String> params = template.getRequestParameters();
		assertThat(params.size(), is(1));
		assertThat(params.get("myParam"), contains("42"));

		// second template
		template = get(flow, 1);
		assertThat(template.getType(), is(equalTo("POST")));
		assertThat(template.getUri(), is(equalTo("/foo/bar.tax")));
		assertThat(template.getBody().getContent(), is(equalTo("test".getBytes())));
		assertThat(template.getBody().getCharset(), is(nullValue()));
		assertThat(template.getRequestHeaders().isEmpty(), is(true));
		assertThat(template.getHeaderExtractions().isEmpty(), is(true));
		assertThat(template.getRequestParameters().isEmpty(), is(true));

		List<DetailExtraction> detailExtractions = template.getDetailExtractions();
		assertThat(detailExtractions.size(), is(equalTo(1)));
		DetailExtraction ed = getOnlyElement(detailExtractions);
		assertThat(ed.getPattern().toString(), is(equalTo("myParamToExtract=([^\"]+)\"")));
		assertThat(ed.getName(), is(equalTo("extractDetail")));
		assertThat(ed.getDefaultValue(), is(nullValue()));
		assertThat(ed.getGroupIndex(), is(equalTo(1)));

		// third template
		template = get(flow, 2);
		assertThat(template.getType(), is(equalTo("POST")));
		assertThat(template.getUri(), is(equalTo("/foo/bar.tax")));
		assertThat(template.getBody().getContent(), is(equalTo("test".getBytes())));
		assertThat(template.getBody().getCharset(), is(equalTo(Charset.forName("UTF-8"))));

		SetMultimap<String, String> headers = template.getRequestHeaders();
		assertThat(headers.size(), is(1));
		assertThat(headers.get("header1"), contains("header1value"));

		params = template.getRequestParameters();
		assertThat(params.size(), is(3));
		assertThat(params.get("param1"), containsInAnyOrder("param1value1", "param1value2"));
		assertThat(params.get("param2"), contains("param<2>value"));

		List<HeaderExtraction> headerExtractions = template.getHeaderExtractions();
		assertThat(headerExtractions.size(), is(equalTo(2)));
		HeaderExtraction he = headerExtractions.get(0);
		assertThat(he.getName(), is(equalTo("header1")));
		assertThat(he.getPlaceholderName(), is(equalTo("header1")));
		he = headerExtractions.get(1);
		assertThat(he.getName(), is(equalTo("header2")));
		assertThat(he.getPlaceholderName(), is(equalTo("myHeader2")));

		detailExtractions = template.getDetailExtractions();
		assertThat(detailExtractions.size(), is(equalTo(2)));

		ed = get(detailExtractions, 0);
		assertThat(ed.getPattern().toString(), is(equalTo("myParamToExtract=([^\"]+)\"")));
		assertThat(ed.getName(), is(equalTo("extractDetail1")));
		assertThat(ed.getDefaultValue(), is(nullValue()));
		assertThat(ed.getGroupIndex(), is(equalTo(1)));

		ed = get(detailExtractions, 1);
		assertThat(ed.getPattern().toString(), is(equalTo("myParamToExtract=([^\"]+)\"")));
		assertThat(ed.getName(), is(equalTo("extractDetail2")));
		assertThat(ed.getDefaultValue(), is(equalTo("mydefault")));
		assertThat(ed.getGroupIndex(), is(equalTo(2)));

		// forth template
		template = get(flow, 3);
		assertThat(template.getType(), is(equalTo("POST")));
		assertThat(template.getUri(), is(equalTo("/foo/bar.tax")));
		headers = template.getRequestHeaders();
		assertThat(headers.size(), is(3));
		assertThat(headers.get("header1"), containsInAnyOrder("header1value1", "header1value2"));
		assertThat(headers.get("header2"), contains("header2value"));

		Body body = template.getBody();
		assertThat(new String(body.getContent(), body.getCharset()), matches("Some multi-line\\s+body content\\s+\\Q^°~+?ß&/%$§@€\\E\\s+blubb"));
		assertThat(body.getCharset(), is(equalTo(Charset.forName("UTF-8"))));
		assertThat(template.getHeaderExtractions().isEmpty(), is(true));
		assertThat(template.getDetailExtractions().isEmpty(), is(true));

		try {
			get(flow, 4);
			fail("Request flow must only contain three request templates.");
		} catch (IndexOutOfBoundsException ex) {
			// expected and thus ignored
		}
	}
}
