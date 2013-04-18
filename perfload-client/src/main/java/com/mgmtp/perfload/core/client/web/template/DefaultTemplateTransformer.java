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
package com.mgmtp.perfload.core.client.web.template;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderUtils;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.Body;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;

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
	 */
	@Override
	public RequestTemplate makeExecutable(final RequestTemplate template, final PlaceholderContainer placeholderContainer) {
		SetMultimap<String, String> requestParameters = template.getRequestParameters();
		SetMultimap<String, String> resolvedParams = HashMultimap.create();

		// Resolve placeholders in parameters
		for (Entry<String, String> entry : requestParameters.entries()) {
			String resolvedKey = PlaceholderUtils.resolvePlaceholders(entry.getKey(), placeholderContainer);
			String resolvedValue = PlaceholderUtils.resolvePlaceholders(entry.getValue(), placeholderContainer);
			resolvedParams.put(resolvedKey, resolvedValue);
		}

		SetMultimap<String, String> requestHeaders = template.getRequestHeaders();
		SetMultimap<String, String> resolvedHeaders = HashMultimap.create();

		// Resolve placeholders in headers
		for (Entry<String, String> entry : requestHeaders.entries()) {
			String resolvedKey = PlaceholderUtils.resolvePlaceholders(entry.getKey(), placeholderContainer);
			String resolvedValue = PlaceholderUtils.resolvePlaceholders(entry.getValue(), placeholderContainer);
			resolvedHeaders.put(resolvedKey, resolvedValue);
		}

		// Resolve placeholders in skip
		String skip = PlaceholderUtils.resolvePlaceholders(template.getSkip(), placeholderContainer);

		// Resolve placeholders in URI
		String uri = PlaceholderUtils.resolvePlaceholders(template.getUri(), placeholderContainer);

		// Resolve placeholders in URI alias
		String uriAlias = PlaceholderUtils.resolvePlaceholders(template.getUriAlias(), placeholderContainer);

		// Resolve placeholders in body, if body is of type text
		Body body = template.getBody();
		if (body != null) {
			byte[] content = body.getContent();
			Charset charset = body.getCharset();
			if (charset != null) {
				String bodyAsString = new String(content, charset);
				bodyAsString = PlaceholderUtils.resolvePlaceholders(bodyAsString, placeholderContainer);
				body = new Body(bodyAsString.getBytes(charset), charset);
			}
		}

		// Resolve placeholders in detail extraction patterns
		List<DetailExtraction> detailExtractions = template.getDetailExtractions();
		List<DetailExtraction> transformedDetailsExtractions = newArrayListWithCapacity(detailExtractions.size());

		for (DetailExtraction extraction : detailExtractions) {
			String name = PlaceholderUtils.resolvePlaceholders(extraction.getName(), placeholderContainer);
			String pattern = PlaceholderUtils.resolvePlaceholders(extraction.getPattern(), placeholderContainer);
			String groupIndexString = PlaceholderUtils
					.resolvePlaceholders(extraction.getGroupIndexString(), placeholderContainer);
			String defaultValue = PlaceholderUtils.resolvePlaceholders(extraction.getDefaultValue(), placeholderContainer);
			String failIfNotFoundString = PlaceholderUtils.resolvePlaceholders(extraction.getFailIfNotFoundString(),
					placeholderContainer);
			String indexedString = PlaceholderUtils.resolvePlaceholders(extraction.getIndexedString(), placeholderContainer);

			DetailExtraction transformedExtraction = new DetailExtraction(name, pattern, groupIndexString, defaultValue,
					indexedString, failIfNotFoundString);
			transformedDetailsExtractions.add(transformedExtraction);
		}

		return new RequestTemplate(template.getType(), skip, uri, uriAlias, resolvedHeaders, resolvedParams, body,
				template.getHeaderExtractions(), transformedDetailsExtractions);
	}
}
