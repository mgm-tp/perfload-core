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
package com.mgmtp.perfload.core.client.lang;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * {@link URLClassLoader} implementation that tries to load classes locally before delegating to the
 * parent class loader. This is necessary for plugin-like architectures where a class and its
 * dependencies need to be loaded in isolation.
 * 
 * @author rnaegele
 */
public final class LocalFirstClassLoader extends URLClassLoader {

	/**
	 * @param urls
	 *            The URLs making up the local classpath for this class loader.
	 */
	public LocalFirstClassLoader(final URL... urls) {
		super(urls);
	}

	/**
	 * Loads the class with the specified binary name trying to load it from the local classpath
	 * first before delegating to the normal class loading mechanism.
	 */
	@Override
	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		// Check if the class has already been loaded
		Class<?> loadedClass = findLoadedClass(name);

		if (loadedClass == null) {
			try {
				// First try to find it locally
				loadedClass = findClass(name);
			} catch (ClassNotFoundException e) {
				// Swallow exception --> the class does not exist locally
			}

			// If the class is not found locally we delegate to the normal class loading mechanism
			if (loadedClass == null) {
				loadedClass = super.loadClass(name, resolve);
			}
		}

		if (resolve) {
			resolveClass(loadedClass);
		}
		return loadedClass;
	}

	/**
	 * Finds the resource with the given name trying to load it from the local classpath first
	 * before delegating to the normal resource loading mechanism.
	 */
	@Override
	public URL getResource(final String name) {
		// First try to find it locally
		URL url = findResource(name);

		// If the resource is not found locally we delegate to the normal resource loading mechanism
		if (url == null) {
			url = super.getResource(name);
		}

		return url;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		URL[] urls = getURLs();
		tsb.append("urls", urls);
		return tsb.toString();
	}
}
