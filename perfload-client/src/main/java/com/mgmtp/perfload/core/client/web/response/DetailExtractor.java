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

import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;

/**
 * Interface for a response detail extractor.
 * 
 * @author rnaegele
 */
public interface DetailExtractor {

	/**
	 * Extracts strings from a response that may later be used to resolve placeholders in
	 * parameterized requests.
	 * 
	 * @param responseInfo
	 *            the {@link ResponseInfo} object wrapping response information
	 * @param detailExtractions
	 *            a list of {@link DetailExtraction} objects that specify what to extract
	 * @param placeholderContainer
	 *            extracted values are stored in this container so they can later be used to resolve
	 *            placeholders
	 * @throws PatternNotFoundException
	 *             if the pattern is not found in the response,
	 *             {@link DetailExtraction#isFailIfNotFound()} returns {@code true} and
	 *             {@link DetailExtraction#getDefaultValue()} returns {@code null}
	 */
	void extractDetails(ResponseInfo responseInfo, List<DetailExtraction> detailExtractions,
			PlaceholderContainer placeholderContainer) throws PatternNotFoundException;
}
