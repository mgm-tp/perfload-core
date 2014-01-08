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
package com.mgmtp.perfload.core.common.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class TestConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int processId;
	private final String guiceModule;
	private final PropertiesMap properties;
	private final List<LoadProfileEvent> loadProfileEvents;

	public TestConfig(final int processId, final String guiceModule, final PropertiesMap properties,
			final Collection<LoadProfileEvent> loadProfileEvents) {
		this.processId = processId;
		this.guiceModule = guiceModule;
		this.properties = properties;
		this.loadProfileEvents = ImmutableList.copyOf(loadProfileEvents);
	}

	/**
	 * @return the processId
	 */
	public int getProcessId() {
		return processId;
	}

	/**
	 * @return the guiceModule
	 */
	public String getGuiceModule() {
		return guiceModule;
	}

	/**
	 * @return the properties
	 */
	public PropertiesMap getProperties() {
		return properties;
	}

	/**
	 * @return the loadProfileEvents
	 */
	public List<LoadProfileEvent> getLoadProfileEvents() {
		return loadProfileEvents;
	}
}
