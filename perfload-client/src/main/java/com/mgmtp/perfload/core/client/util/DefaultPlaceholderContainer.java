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
package com.mgmtp.perfload.core.client.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.collect.ForwardingMap;
import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;

/**
 * Default {@link PlaceholderContainer} implementation.
 * 
 * @author rnaegele
 */
@NotThreadSafe
@ThreadScoped
public class DefaultPlaceholderContainer extends ForwardingMap<String, String> implements PlaceholderContainer {
	private final Map<String, String> placeholders = newHashMap();

	@Override
	protected Map<String, String> delegate() {
		return placeholders;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param key
	 *            key with which the specified value is to be associated; must be non-{@code null}
	 * @param value
	 *            to be associated with the specified key; must be non-{@code null}
	 */
	@Override
	public String put(final String key, final String value) {
		checkArgument(key != null, "Parameter 'key' must not be null.");
		checkArgument(value != null, "Parameter 'value' must not be null.");

		return placeholders.put(key, value);
	}
}
