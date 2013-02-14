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
package com.mgmtp.perfload.core.common.config;

import java.io.Serializable;

import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Base class for testplan configurations.
 * 
 * @author rnaegele
 */
public abstract class AbstractTestplanConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String testplanId;
	private final String guiceModule;
	private final PropertiesMap properties = new PropertiesMap();

	/**
	 * @param testplanId
	 *            the id fo the testplan
	 * @param guiceModule
	 *            the fully qualified classname of the Guice module for the test
	 */
	public AbstractTestplanConfig(final String testplanId, final String guiceModule) {
		this.testplanId = testplanId;
		this.guiceModule = guiceModule;
	}

	/**
	 * @return the testplan id
	 */
	public String getTestplanId() {
		return testplanId;
	}

	/**
	 * @return the fully qualified classname of the Guice module used to configure the test
	 */
	public String getGuiceModule() {
		return guiceModule;
	}

	/**
	 * @return the fully qualified classname of the Guice module used to configure the test
	 */
	public PropertiesMap getProperties() {
		return new PropertiesMap(properties);
	}

	public String putProperty(final String key, final String value) {
		return properties.put(key, value);
	}

	/**
	 * @return the number of threads
	 */
	public abstract int getThreads();
}
