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
package com.mgmtp.perfload.test.utils;

import com.google.inject.Injector;
import com.mgmtp.perfload.core.client.config.ModulesLoader;
import com.mgmtp.perfload.core.client.runner.LtRunner;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Utility class for running a load test driver within a unit test.
 * 
 * @author rnaegele
 * @since 4.7.0
 */
public class DriverTestRunner {

	public static void runDriver(final AbstractWebLtModule driverModule, final String operation, final String target)
			throws Exception {
		PropertiesMap properties = driverModule.getProperties();
		properties.put("wtm.strategy.constant.waitingTimeMillis", "0");
		properties.put("wtm.beforeTestStartMillis", "0");

		ModulesLoader ml = new ModulesLoader(new DriverTestModule(driverModule, properties, operation, target), properties,
				new MockClient(), 1, 1);

		Injector inj = ml.createInjector();
		LtRunner runner = inj.getInstance(LtRunner.class);
		runner.execute();
	}
}
