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
package com.mgmtp.perfload.core.common.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * Utility class for loading properties into a {@link PropertiesMap}. Properties are loaded using
 * {@link Properties#load(Reader)} and then copied into a {@link PropertiesMap} using
 * {@link PropertiesMap#fromProperties(Properties)}.
 * 
 * @author rnaegele
 */
public final class PropertiesUtils {

	private PropertiesUtils() {
		//
	}

	/**
	 * Loads properties as map from the given classpath resource using the current context
	 * classloader. This method delegates to {@link #loadProperties(Reader)}.
	 * 
	 * @param resource
	 *            the resource
	 * @param encoding
	 *            the encoding to use
	 * @param exceptionIfNotFound
	 *            if {@code true}, an {@link IllegalStateException} is thrown if the resource cannot
	 *            be loaded; otherwise an empty map is returned
	 * @return the map
	 */
	public static PropertiesMap loadProperties(final String resource, final String encoding,
			final boolean exceptionIfNotFound) throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Reader reader = null;
		try {
			InputStream is = cl.getResourceAsStream(resource);
			if (is == null) {
				if (exceptionIfNotFound) {
					throw new IOException("InputStream is null. Properties not found: " + resource);
				}
				return new PropertiesMap();
			}
			reader = new InputStreamReader(is, encoding);
			return loadProperties(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Loads properties as map from the specified reader. This method delegates to
	 * {@link Properties#load(Reader)}.
	 * 
	 * @return the map
	 */
	public static PropertiesMap loadProperties(final Reader reader) throws IOException {
		Properties props = new Properties();
		props.load(reader);
		return PropertiesMap.fromProperties(props);
	}

	public static List<String> getSubList(final PropertiesMap properties, final String keyPrefix) {
		String prefix = keyPrefix.endsWith(".") ? keyPrefix : keyPrefix + ".";

		List<String> result = newArrayList();
		for (int i = 1;; ++i) {
			String value = properties.get(prefix + i);
			if (value == null) {
				break;
			}
			result.add(value);
		}
		return result;
	}

	public static Map<String, String> getSubMap(final PropertiesMap properties, final String keyPrefix) {
		final String prefix = keyPrefix.endsWith(".") ? keyPrefix : keyPrefix + ".";
		Map<String, String> map = Maps.filterKeys(properties, new Predicate<String>() {
			@Override
			public boolean apply(final String input) {
				return input.startsWith(prefix);
			}
		});

		Map<String, String> result = newHashMapWithExpectedSize(map.size());
		for (Entry<String, String> entry : map.entrySet()) {
			result.put(substringAfter(entry.getKey(), prefix), entry.getValue());
		}
		return result;
	}
}
