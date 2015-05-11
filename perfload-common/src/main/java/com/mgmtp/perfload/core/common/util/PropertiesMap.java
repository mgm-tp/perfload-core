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
package com.mgmtp.perfload.core.common.util;

import static com.google.common.collect.Iterators.asEnumeration;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.MapMaker;

/**
 * An alternative to {@link Properties}. Does not suffer from the disadvantages of
 * {@link Properties} in that it has proper generic types ({@link String}) and does not inherit from
 * {@link Hashtable} (nor any other JDK collection/map). This class is thread-safe using a
 * {@link ConcurrentMap} internally.
 * 
 * @author rnaegele
 */
@ThreadSafe
public final class PropertiesMap extends ForwardingMap<String, String> implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<String, String> delegate;

	/**
	 * Creates a new empty instance.
	 */
	public PropertiesMap() {
		this(Collections.<String, String>emptyMap());
	}

	/**
	 * Creates a new instance pre-populated with properties from the specified map.
	 * 
	 * @param initialProps
	 *            a map of initial properties
	 */
	public PropertiesMap(final Map<String, String> initialProps) {
		ConcurrentMap<String, String> map = new MapMaker().makeMap();
		map.putAll(initialProps);
		this.delegate = map;
	}

	/**
	 * Creates a new {@link PropertiesMap} from the given {@link Properties} instance.
	 * 
	 * @param props
	 *            the properties instance
	 * @return the {@link PropertiesMap}
	 */
	public static PropertiesMap fromProperties(final Properties props) {
		PropertiesMap map = new PropertiesMap();
		for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
			String propertyName = (String) e.nextElement();
			String value = props.getProperty(propertyName);
			map.put(propertyName, value);
		}
		return map;
	}

	/**
	 * Converts this {@link PropertiesMap} to a {@link Properties} instance.
	 * 
	 * @return the {@link Properties} instance
	 */
	public Properties toProperties() {
		return toProperties(false);
	}

	/**
	 * Converts this {@link PropertiesMap} to a {@link Properties} instance.
	 * 
	 * @param sorted
	 *            if {@code true}, an anonymous {@link Properties} subclass is return whose
	 *            {@link Properties#keys() keys()} method is overridden such that keys are sorted
	 *            (e. g. useful when properties are saved to a file)
	 * @return the {@link Properties} instance
	 */
	public Properties toProperties(final boolean sorted) {
		Properties props = new SortedProperties(sorted);
		props.putAll(this);
		return props;
	}

	@Override
	protected ConcurrentMap<String, String> delegate() {
		return delegate;
	}

	/**
	 * Searches for the property with the specified key. The method returns {@code null} if the
	 * property is not found.
	 * 
	 * @return the property value
	 */
	@Override
	public String get(final Object key) {
		String value = super.get(key);
		log.info("get(key={}) returns: {}", key, value);
		return value;
	}

	/**
	 * Searches for the property with the specified key. The method returns the default value
	 * argument if the property is not found.
	 * 
	 * @see PropertiesMap#get(Object)
	 * @return the property value
	 */
	public String get(final Object key, final String defaultValue) {
		String value = get(key);
		if (value == null) {
			value = defaultValue;
		}
		log.info("get(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, value });
		return value;
	}

	/**
	 * @see PropertiesMap#get(Object)
	 * @return the property value as Integer
	 */
	public Integer getInteger(final String key) {
		return getInteger(key, null);
	}

	/**
	 * @see PropertiesMap#get(Object, String)
	 * @return the property value as Integer
	 */
	public Integer getInteger(final String key, final Integer defaultValue) {
		String value = get(key);
		Integer result = value != null ? Integer.valueOf(value) : defaultValue;
		log.info("getInteger(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, result });
		return result;
	}

	/**
	 * @see PropertiesMap#get(Object)
	 * @return the property value as Long
	 */
	public Long getLong(final String key) {
		return getLong(key, null);
	}

	/**
	 * @see PropertiesMap#get(Object, String)
	 * @return the property value as Long
	 */
	public Long getLong(final String key, final Long defaultValue) {
		String value = get(key);
		Long result = value != null ? Long.valueOf(value) : defaultValue;
		log.info("getLong(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, result });
		return result;
	}

	/**
	 * @see PropertiesMap#get(Object)
	 * @return the property value as Double
	 */
	public Double getDouble(final String key) {
		return getDouble(key, null);
	}

	/**
	 * @see PropertiesMap#get(Object, String)
	 * @return the property value as Double
	 */
	public Double getDouble(final String key, final Double defaultValue) {
		String value = get(key);
		Double result = value != null ? Double.valueOf(value) : defaultValue;
		log.info("getDouble(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, result });
		return result;
	}

	/**
	 * @see PropertiesMap#get(Object)
	 * @return the property value as Float
	 */
	public Float getFloat(final String key) {
		return getFloat(key, null);
	}

	/**
	 * @see PropertiesMap#get(Object, String)
	 * @return the property value as Float
	 */
	public Float getFloat(final String key, final Float defaultValue) {
		String value = get(key);
		Float result = value != null ? Float.valueOf(value) : defaultValue;
		log.info("getFloat(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, result });
		return result;
	}

	/**
	 * @see PropertiesMap#get(Object)
	 * @return the property value as Boolean
	 */
	public Boolean getBoolean(final String key) {
		return getBoolean(key, Boolean.FALSE);
	}

	/**
	 * @see PropertiesMap#get(Object, String)
	 * @return the property value as Boolean
	 */
	public Boolean getBoolean(final String key, final Boolean defaultValue) {
		String value = get(key);
		Boolean result = value != null ? Boolean.valueOf(value) : defaultValue;
		log.info("getBoolean(key={}, defaultValue={}) returns: {}", new Object[] { key, defaultValue, result });
		return result;
	}

	static final class SortedProperties extends Properties {
		private static final long serialVersionUID = 1L;
		private final boolean sorted;

		SortedProperties(final boolean sorted) {
			this.sorted = sorted;
		}

		@Override
		public synchronized Enumeration<Object> keys() {
			if (!sorted) {
				return super.keys();
			}

			Set<Object> sortedSet = new TreeSet<>();
			for (Enumeration<Object> en = super.keys(); en.hasMoreElements();) {
				sortedSet.add(en.nextElement());
			}
			return asEnumeration(sortedSet.iterator());
		}
	}
}
