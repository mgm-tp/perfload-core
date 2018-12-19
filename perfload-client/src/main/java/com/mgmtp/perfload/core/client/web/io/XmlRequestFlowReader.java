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
package com.mgmtp.perfload.core.client.web.io;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;
import com.mgmtp.perfload.core.common.xml.Dom4jReader;

/**
 * Reads the request flow from an XML file.
 *
 * @author rnaegele
 */
public final class XmlRequestFlowReader {

	private static final String SCHEMA_RESOURCE = "perfload-request-flow.xsd";

	private final String resourcePath;
	private final String resourceName;

	/**
	 *
	 * @param resourcePath
	 *            the path to the request flow XML resource on the classpath
	 * @param resourceName
	 *            the name of the XML request flow resource
	 */
	public XmlRequestFlowReader(final String resourcePath, final String resourceName) {
		this.resourcePath = resourcePath;
		this.resourceName = resourceName;
	}

	/**
	 * Reads the XML resource transforming it into an objewct tree.
	 *
	 * @return the request flow instance
	 */
	public RequestFlow readFlow() throws ParserConfigurationException, SAXException, DocumentException {
		Element root = loadDocument().getRootElement();

		@SuppressWarnings("unchecked")
		List<Element> requests = root.elements();
		List<RequestTemplate> templates = newArrayListWithCapacity(requests.size());

		for (Element requestElem : requests) {
			String id = emptyToNull(requestElem.attributeValue("id"));
			String type = requestElem.attributeValue("type");
			String skip = defaultString(emptyToNull(requestElem.attributeValue("skip")), "false");
			String uri = requestElem.attributeValue("uri");
			String uriAlias = emptyToNull(requestElem.attributeValue("uriAlias"));
			String validateResponse = defaultString(emptyToNull(requestElem.attributeValue("validateResponse")), "true");

			@SuppressWarnings("unchecked")
			List<Element> params = requestElem.elements("param");
			SetMultimap<String, String> paramsMultiMap = HashMultimap.create(params.size(), 3);
			for (Element paramElem : params) {
				String key = paramElem.attributeValue("name");
				String value = paramElem.getText();
				paramsMultiMap.put(key, value);
			}

			@SuppressWarnings("unchecked")
			List<Element> headers = requestElem.elements("header");
			SetMultimap<String, String> headersMultiMap = HashMultimap.create(headers.size(), 3);
			for (Element headerElem : headers) {
				String key = headerElem.attributeValue("name");
				String value = headerElem.getText();
				headersMultiMap.put(key, value);
			}

			Element bodyElement = requestElem.element("body");
			Body body = null;
			if (bodyElement != null) {
				String bodyContent = emptyToNull(bodyElement.getText());
				String resPath = bodyElement.attributeValue("resourcePath");
				String resourceType = bodyElement.attributeValue("resourceType");

				checkState(bodyContent != null ^ (resPath != null && resourceType != null),
						"Must specify either body content or resource path and type. [" + requestElem.asXML() + "]");

				if (bodyContent != null) {
					body = Body.create(bodyContent);
				} else {
					body = Body.create(resPath, resourceType);
				}
			}

			@SuppressWarnings("unchecked")
			List<Element> headerExtractions = requestElem.elements("headerExtraction");
			List<HeaderExtraction> extractHeadersList = newArrayListWithCapacity(headerExtractions.size());
			for (Element extractHeaderElem : headerExtractions) {
				String name = extractHeaderElem.attributeValue("name");
				String placeholderName = extractHeaderElem.attributeValue("placeholderName");
				extractHeadersList.add(new HeaderExtraction(name, placeholderName));
			}

			@SuppressWarnings("unchecked")
			List<Element> detailExtractions = requestElem.elements("detailExtraction");
			List<DetailExtraction> extractDetailsList = newArrayListWithCapacity(detailExtractions.size());
			for (Element extractDetailElem : detailExtractions) {
				String name = extractDetailElem.attributeValue("name");
				String groupIndexString = extractDetailElem.attributeValue("groupIndex");
				String defaultValue = extractDetailElem.attributeValue("defaultValue");
				String indexedString = extractDetailElem.attributeValue("indexed");
				String failIfNotFoundString = extractDetailElem.attributeValue("failIfNotFound");
				String pattern = extractDetailElem.getText().trim();
				DetailExtraction ed = new DetailExtraction(name, pattern, groupIndexString, defaultValue,
						indexedString, failIfNotFoundString);
				extractDetailsList.add(ed);
			}

			templates.add(new RequestTemplate(id, type, skip, uri, uriAlias, headersMultiMap, paramsMultiMap, body,
					extractHeadersList, extractDetailsList, validateResponse));
		}

		return new RequestFlow(resourceName, templates);
	}

	private Document loadDocument() throws ParserConfigurationException, SAXException, DocumentException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String schemaUrl = loader.getResource(SCHEMA_RESOURCE).toString();
		String resource = resourcePath + resourceName;
		String xmlResourceUrl = loader.getResource(resource).toString();
		return Dom4jReader.loadDocument(new InputSource(xmlResourceUrl), new StreamSource(schemaUrl), "UTF-8");
	}
}
