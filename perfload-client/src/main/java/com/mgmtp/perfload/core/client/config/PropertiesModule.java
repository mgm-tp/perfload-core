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
package com.mgmtp.perfload.core.client.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Guice module for binding a {@link PropertiesMap}. Binds the map as a whole and, additionally,
 * every property value with the annotation {@code @Named("<propKey>")}.
 * 
 * @author rnaegele
 */
final class PropertiesModule extends AbstractModule {

	private final PropertiesMap properties;

	/**
	 * Creates a new instance with the specified properties.
	 * 
	 * @param properties
	 *            properties for the test
	 */
	public PropertiesModule(final PropertiesMap properties) {
		this.properties = properties;
	}

	/**
	 * Binds the {@link PropertiesMap} as a whole and, additionally, every single property value
	 * with the annotation {@code @Named("<propKey>")}.
	 */
	@Override
	protected void configure() {
		bind(PropertiesMap.class).toInstance(properties);
		Names.bindProperties(binder(), properties);
	}
}
