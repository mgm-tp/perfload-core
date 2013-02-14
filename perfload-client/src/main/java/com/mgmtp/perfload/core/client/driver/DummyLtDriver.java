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
package com.mgmtp.perfload.core.client.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy driver for testing purposes that can be used to check the setup of the system.
 * 
 * @author rnaegele
 */
public class DummyLtDriver implements LtDriver {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Writes log messages and a time measurement only.
	 */
	@Override
	public void execute() throws Exception {
		log.warn("DummyDriver!!!");
	}
}
