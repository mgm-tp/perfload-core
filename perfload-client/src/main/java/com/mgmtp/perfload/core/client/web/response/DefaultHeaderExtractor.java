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

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;

/**
 * Default header extractor implementation.
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public class DefaultHeaderExtractor implements HeaderExtractor {
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * {@inheritDoc}
	 * 
	 * @see HeaderExtraction#HeaderExtraction(String, String)
	 */
	@Override
	public void extractHeaders(final ResponseInfo responseInfo, final List<HeaderExtraction> headerExtractions,
			final PlaceholderContainer placeholderContainer) {
		log.debug("Extracting headers from response...");

		for (HeaderExtraction headerExtraction : headerExtractions) {
			String name = headerExtraction.getName();
			String value = responseInfo.getHeaders().get(name);
			log.debug("Extracting header '{}={}'", name, value);

			String placeholderName = headerExtraction.getPlaceholderName();
			placeholderContainer.put(placeholderName, value);
		}
	}
}
