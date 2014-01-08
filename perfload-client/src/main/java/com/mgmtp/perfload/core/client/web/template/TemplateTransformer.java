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
package com.mgmtp.perfload.core.client.web.template;

import java.io.IOException;

import com.mgmtp.perfload.core.client.util.PlaceholderContainer;

/**
 * Class for transforming a {@link RequestTemplate request template} such that it is executable, i.
 * e. with all placeholder tokens resolved.
 * 
 * @author rnaegele
 */
public interface TemplateTransformer {

	/**
	 * Creates an executable copy of the specified template with all placeholders tokens resolved.
	 * 
	 * @param template
	 *            the template to transform
	 * @param placeholderContainer
	 *            the placeholder container
	 * @return the executable template
	 * @throws IOException
	 *             can be thrown if the request contains body content that is loaded from a
	 *             classpath resource
	 */
	RequestTemplate makeExecutable(RequestTemplate template, PlaceholderContainer placeholderContainer) throws IOException;
}
