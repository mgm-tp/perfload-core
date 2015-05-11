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

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static com.mgmtp.perfload.core.client.util.PlaceholderUtils.resolvePlaceholders;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderUtils;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;

/**
 * Default implementation of a {@link TemplateTransformer}.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public final class DefaultTemplateTransformer implements TemplateTransformer {

	/**
	 * Creates a new {@link RequestTemplate} based on the specified one replacing placeholder tokens
	 * with their resolved values from the specified {@link PlaceholderContainer}. Placeholder
	 * tokens in uri, parameter keys, parameter values, and detail extraction patterns are subject
	 * to resolution.
	 * 
	 * @param template
	 *            the parameterized request template
	 * @param placeholderContainer
	 *            the placeholder container for resolving placeholder tokens from
	 * @throws IOException
	 *             can be thrown if the request contains body content that is loaded from a
	 *             classpath resource
	 */
	@Override
	public RequestTemplate makeExecutable(final RequestTemplate template, final PlaceholderContainer placeholderContainer)
			throws IOException {
		String type = resolvePlaceholders(template.getType(), placeholderContainer);

		SetMultimap<String, String> requestParameters = template.getRequestParameters();
		SetMultimap<String, String> resolvedParams = HashMultimap.create();

		// Resolve placeholders in parameters
		for (Entry<String, String> entry : requestParameters.entries()) {
			String resolvedKey = resolvePlaceholders(entry.getKey(), placeholderContainer);
			String resolvedValue = resolvePlaceholders(entry.getValue(), placeholderContainer);
			resolvedParams.put(resolvedKey, resolvedValue);
		}

		SetMultimap<String, String> requestHeaders = template.getRequestHeaders();
		SetMultimap<String, String> resolvedHeaders = HashMultimap.create();

		// Resolve placeholders in headers
		for (Entry<String, String> entry : requestHeaders.entries()) {
			String resolvedKey = resolvePlaceholders(entry.getKey(), placeholderContainer);
			String resolvedValue = resolvePlaceholders(entry.getValue(), placeholderContainer);
			resolvedHeaders.put(resolvedKey, resolvedValue);
		}

		// Resolve placeholders in skip
		String skip = resolvePlaceholders(template.getSkip(), placeholderContainer);

		// Resolve placeholders in URI
		String uri = resolvePlaceholders(template.getUri(), placeholderContainer);

		// Resolve placeholders in URI alias
		String uriAlias = resolvePlaceholders(template.getUriAlias(), placeholderContainer);

		// Resolve placeholders in validateResponse
		String validateResponse = resolvePlaceholders(template.getValidateResponse(), placeholderContainer);

		// Resolve placeholders in body, if body is of type text
		Body body = template.getBody();
		if (body != null) {
			byte[] content = body.getContent();
			if (content != null) {
				// content comes from request flow and is always considered UTF-8
				String bodyAsString = new String(content, Charsets.UTF_8);
				bodyAsString = resolvePlaceholders(bodyAsString, placeholderContainer);
				body = Body.create(bodyAsString);
			} else {
				String resourcePath = resolvePlaceholders(body.getResourcePath(), placeholderContainer);
				ResourceType resourceType = ResourceType
						.valueOf(resolvePlaceholders(body.getResourceType(), placeholderContainer));
				byte[] byteContent = toByteArray(getResource(resourcePath));
				switch (resourceType) {
					case text:
						String stringContent = resolvePlaceholders(new String(byteContent, Charsets.UTF_8), placeholderContainer);
						body = Body.create(stringContent);
						break;
					case binary:
						body = Body.create(byteContent);
						break;
					default:
						throw new IllegalStateException("Invalid resource type: " + resourceType);
				}
			}
		}

		// Resolve placeholders in detail extraction patterns
		List<DetailExtraction> detailExtractions = template.getDetailExtractions();
		List<DetailExtraction> transformedDetailExtractions = newArrayListWithCapacity(detailExtractions.size());

		for (DetailExtraction extraction : detailExtractions) {
			String name = resolvePlaceholders(extraction.getName(), placeholderContainer);
			String pattern = resolvePlaceholders(extraction.getPattern(), placeholderContainer);
			String groupIndexString = PlaceholderUtils
					.resolvePlaceholders(extraction.getGroupIndexString(), placeholderContainer);
			String defaultValue = resolvePlaceholders(extraction.getDefaultValue(), placeholderContainer);
			String failIfNotFoundString = resolvePlaceholders(extraction.getFailIfNotFoundString(),
					placeholderContainer);
			String indexedString = resolvePlaceholders(extraction.getIndexedString(), placeholderContainer);

			DetailExtraction transformedExtraction = new DetailExtraction(name, pattern, groupIndexString, defaultValue,
					indexedString, failIfNotFoundString);
			transformedDetailExtractions.add(transformedExtraction);
		}

		// Resolve placeholders in detail extraction patterns
		List<HeaderExtraction> headerExtractions = template.getHeaderExtractions();
		List<HeaderExtraction> transformedHeaderExtractions = newArrayListWithCapacity(headerExtractions.size());

		for (HeaderExtraction extraction : headerExtractions) {
			String name = resolvePlaceholders(extraction.getName(), placeholderContainer);
			String placeholderName = resolvePlaceholders(extraction.getPlaceholderName(), placeholderContainer);

			HeaderExtraction transformedExtraction = new HeaderExtraction(name, placeholderName);
			transformedHeaderExtractions.add(transformedExtraction);
		}

		return new RequestTemplate(type, skip, uri, uriAlias, resolvedHeaders, resolvedParams, body,
				transformedHeaderExtractions, transformedDetailExtractions, validateResponse);
	}
}
