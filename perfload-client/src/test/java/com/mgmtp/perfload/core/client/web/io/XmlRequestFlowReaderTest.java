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
package com.mgmtp.perfload.core.client.web.io;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;
import com.mgmtp.perfload.core.client.web.template.ResourceType;

/**
 * @author rnaegele
 */
public class XmlRequestFlowReaderTest {

	@Test
	public void testReader() throws Exception {
		XmlRequestFlowReader reader = new XmlRequestFlowReader("", "request-flow.xml");
		RequestFlow flow = reader.readFlow();

		// first template
		RequestTemplate template = get(flow, 0);
		assertThat(template.getType()).isEqualTo("GET");
		assertThat(template.getUri()).isEqualTo("/foo/");
		assertThat(template.getBody()).isNull();
		assertThat(template.getDetailExtractions().isEmpty()).isTrue();
		assertThat(template.getHeaderExtractions().isEmpty()).isTrue();
		assertThat(template.getRequestHeaders().isEmpty()).isTrue();

		SetMultimap<String, String> params = template.getRequestParameters();
		assertThat(params.size()).isEqualTo(1);
		assertThat(params.get("myParam")).contains("42");

		// second template
		template = get(flow, 1);
		assertThat(template.getType()).isEqualTo("POST");
		assertThat(template.getUri()).isEqualTo("/foo/bar.tax");
		assertThat(template.getBody().getContent()).isNull();
		assertThat(template.getBody().getResourcePath()).isEqualTo("fooResource");
		assertThat(template.getBody().getResourceType()).isEqualTo(ResourceType.binary.name());
		assertThat(template.getRequestHeaders().isEmpty()).isTrue();
		assertThat(template.getHeaderExtractions().isEmpty()).isTrue();
		assertThat(template.getRequestParameters().isEmpty()).isTrue();

		List<DetailExtraction> detailExtractions = template.getDetailExtractions();
		assertThat(detailExtractions).hasSize(1);
		DetailExtraction ed = getOnlyElement(detailExtractions);
		assertThat(ed.getPattern().toString()).isEqualTo("myParamToExtract=([^\"]+)\"");
		assertThat(ed.getName()).isEqualTo("extractDetail");
		assertThat(ed.getDefaultValue()).isNull();
		assertThat(ed.getGroupIndex()).isEqualTo(1);

		// third template
		template = get(flow, 2);
		assertThat(template.getType()).isEqualTo("POST");
		assertThat(template.getUri()).isEqualTo("/foo/bar.tax");
		assertThat(template.getBody().getContent()).isNull();
		assertThat(template.getBody().getResourcePath()).isEqualTo("fooResource");
		assertThat(template.getBody().getResourceType()).isEqualTo(ResourceType.text.name());

		SetMultimap<String, String> headers = template.getRequestHeaders();
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.get("header1")).contains("header1value");

		params = template.getRequestParameters();
		assertThat(params.size()).isEqualTo(3);
		assertThat(params.get("param1")).contains("param1value1", "param1value2");
		assertThat(params.get("param2")).contains("param<2>value");

		List<HeaderExtraction> headerExtractions = template.getHeaderExtractions();
		assertThat(headerExtractions.size()).isEqualTo(2);
		HeaderExtraction he = headerExtractions.get(0);
		assertThat(he.getName()).isEqualTo("header1");
		assertThat(he.getPlaceholderName()).isEqualTo("header1");
		he = headerExtractions.get(1);
		assertThat(he.getName()).isEqualTo("header2");
		assertThat(he.getPlaceholderName()).isEqualTo("myHeader2");

		detailExtractions = template.getDetailExtractions();
		assertThat(detailExtractions.size()).isEqualTo(2);

		ed = get(detailExtractions, 0);
		assertThat(ed.getPattern().toString()).isEqualTo("myParamToExtract=([^\"]+)\"");
		assertThat(ed.getName()).isEqualTo("extractDetail1");
		assertThat(ed.getDefaultValue()).isNull();
		assertThat(ed.getGroupIndex()).isEqualTo(1);

		ed = get(detailExtractions, 1);
		assertThat(ed.getPattern().toString()).isEqualTo("myParamToExtract=([^\"]+)\"");
		assertThat(ed.getName()).isEqualTo("extractDetail2");
		assertThat(ed.getDefaultValue()).isEqualTo("mydefault");
		assertThat(ed.getGroupIndex()).isEqualTo(2);

		// forth template
		template = get(flow, 3);
		assertThat(template.getType()).isEqualTo("POST");
		assertThat(template.getUri()).isEqualTo("/foo/bar.tax");
		headers = template.getRequestHeaders();
		assertThat(headers.size()).isEqualTo(3);
		assertThat(headers.get("header1")).contains("header1value1", "header1value2");
		assertThat(headers.get("header2")).contains("header2value");

		Body body = template.getBody();
		assertThat(new String(body.getContent(), Charsets.UTF_8))
				.matches("Some multi-line\\s+body content\\s+\\Q^°~+?ß&/%$§@€\\E\\s+blubb");
		assertThat(body.getResourceType()).isEqualTo(ResourceType.text.name());
		assertThat(template.getHeaderExtractions().isEmpty()).isTrue();
		assertThat(template.getDetailExtractions().isEmpty()).isTrue();

		try {
			get(flow, 4);
			fail("Request flow must only contain three request templates.");
		} catch (IndexOutOfBoundsException ex) {
			// expected and thus ignored
		}
	}
}
