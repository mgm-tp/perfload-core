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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.driver.ProcessInfo;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class LtProcessModuleTest {

	@Test
	public void testProvideProcessInfo() {
		PropertiesMap props = new PropertiesMap();
		props.put("operation.test.procInfo.dir", "dir");
		props.put("operation.test.procInfo.envVars.ENV_VAR_1", "envVar1");
		props.put("operation.test.procInfo.envVars.ENV_VAR_2", "envVar2");
		props.put("operation.test.procInfo.commands.1", "java -jar foo.jar -param=foo");
		props.put("operation.test.procInfo.redirectProcessOutput", "true");
		props.put("operation.test.procInfo.logPrefix", "prefix>");

		LtProcessModule module = new LtProcessModule(null, null, 0, 0);

		ProcessInfo processInfo = module.provideProcessInfo("test", props);
		assertEquals(processInfo.getDirectory(), "dir");
		assertThat(processInfo.getEnvVars()).contains(entry("ENV_VAR_1", "envVar1"));
		assertThat(processInfo.getEnvVars()).contains(entry("ENV_VAR_2", "envVar2"));
		assertThat(processInfo.getCommands()).contains("java -jar foo.jar -param=foo");
		assertThat(processInfo.isRedirectProcessOutput()).isTrue();
		assertThat(processInfo.getLogPrefix()).isEqualTo("prefix>");
	}
}
