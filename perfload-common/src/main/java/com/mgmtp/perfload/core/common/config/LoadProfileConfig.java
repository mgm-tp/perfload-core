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

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Testplan configuration implementation for a test with a load profile.
 * 
 * @author rnaegele
 */
public class LoadProfileConfig extends AbstractTestplanConfig {
	private static final long serialVersionUID = 1L;

	private final List<LoadProfileEvent> loadProfileEvents;

	/**
	 * @param testplanId
	 *            the id fo the testplan
	 * @param guiceModule
	 *            the fully qualified classname of the Guice module for the test
	 * @param loadProfileEvents
	 *            the list of load profile events for the test
	 */
	public LoadProfileConfig(final String testplanId, final String guiceModule, final List<LoadProfileEvent> loadProfileEvents) {
		super(testplanId, guiceModule);
		this.loadProfileEvents = ImmutableList.copyOf(loadProfileEvents);
	}

	public List<LoadProfileEvent> getLoadProfileEvents() {
		return loadProfileEvents;
	}

	@Override
	public int getThreads() {
		return loadProfileEvents.size();
	}
}
