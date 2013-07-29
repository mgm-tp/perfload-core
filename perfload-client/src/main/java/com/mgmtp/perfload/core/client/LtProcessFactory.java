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
package com.mgmtp.perfload.core.client;

import com.mgmtp.perfload.core.common.config.TestConfig;

/**
 * Factory interface for {@link LtProcess}. The interface is dynamically implemented by a Guice
 * Proxy using Guice's Assisted Inject feature.
 * 
 * @author rnaegele
 */
public interface LtProcessFactory {

	/**
	 * Creates a new LtProcess instance.
	 * 
	 * @param config
	 *            the test configuration for the test process
	 * @return The newly created LtProcess instance
	 */
	LtProcess create(TestConfig config);
}
