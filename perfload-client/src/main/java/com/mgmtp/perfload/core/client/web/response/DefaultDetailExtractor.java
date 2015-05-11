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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;

/**
 * Default detail extractor implementation.
 * 
 * @author rnaegele
 * @since 4.7.0
 */
@Singleton
@ThreadSafe
@Immutable
public class DefaultDetailExtractor implements DetailExtractor {
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * {@inheritDoc}
	 * 
	 * @see DetailExtraction#DetailExtraction(String, String, String, String, String, String)
	 */
	@Override
	public void extractDetails(final ResponseInfo responseInfo, final List<DetailExtraction> detailExtractions,
			final PlaceholderContainer placeholderContainer) throws PatternNotFoundException {
		log.debug("Extracting details from response...");

		for (DetailExtraction detailExtraction : detailExtractions) {
			String name = detailExtraction.getName();
			boolean indexed = detailExtraction.isIndexed();
			String regex = detailExtraction.getPattern();
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(responseInfo.getBodyAsString());

			boolean found = false;
			for (int i = 0;; ++i) {
				if (matcher.find()) {
					found = true;
					String extractedValue = matcher.group(detailExtraction.getGroupIndex());
					if (indexed) {
						// multiple matches possible, so don't break out of loop
						String indexedName = name + "#" + i;
						log.debug("Extracted indexed detail '{}': {}", indexedName, extractedValue);
						placeholderContainer.put(indexedName, extractedValue);
						responseInfo.addDetailExtractionName(indexedName);
					} else {
						log.debug("Extracted detail '{}': {}", name, extractedValue);
						placeholderContainer.put(name, extractedValue);
						responseInfo.addDetailExtractionName(name);
						break;
					}
				} else {
					break;
				}
			}
			if (!found) {
				String defaultValue = detailExtraction.getDefaultValue();
				if (defaultValue != null) {
					if (indexed) {
						String indexedName = name + "#0";
						log.info("Detail '{}' not found in response. Using default indexed value: {}", indexedName, defaultValue);
						placeholderContainer.put(indexedName, defaultValue);
						responseInfo.addDetailExtractionName(indexedName);
					} else {
						log.info("Detail '{}' not found in response. Using default value: {}", name, defaultValue);
						placeholderContainer.put(name, defaultValue);
						responseInfo.addDetailExtractionName(name);
					}
				} else if (detailExtraction.isFailIfNotFound()) {
					throw new PatternNotFoundException("Pattern '" + pattern
							+ "' not found in response and no default value set!");
				}
			}
		}
	}
}
