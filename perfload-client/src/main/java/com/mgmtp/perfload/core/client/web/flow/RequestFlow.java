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
package com.mgmtp.perfload.core.client.web.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;

/**
 * Represents a request flow. A request flow contains a list of request templates.
 * 
 * @author rnaegele
 */
public final class RequestFlow implements Iterable<RequestTemplate> {

	private final List<RequestTemplate> requestTemplates;
	private final String resourceName;

	/**
	 * Constructs a new instance with the specified list of request templates.
	 * 
	 * @param resourceName
	 *            the name of the request flow XML resource
	 * @param requestTemplates
	 *            a list of request templates; must not be null or empty
	 */
	public RequestFlow(final String resourceName, final List<RequestTemplate> requestTemplates) {
		checkArgument(resourceName != null, "'resourceName' not be null");
		checkArgument(requestTemplates != null && !requestTemplates.isEmpty(), "'requestTemplates' must not be null or empty");
		this.resourceName = resourceName;
		this.requestTemplates = ImmutableList.copyOf(requestTemplates);
	}

	/**
	 * Returns an unmodifiable iterator over the request templates.
	 */
	@Override
	public Iterator<RequestTemplate> iterator() {
		return requestTemplates.iterator();
	}

	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
