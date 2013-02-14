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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.driver.ProcessInfo;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class LtProcessModuleTest {

	@Test
	public void testProvideProcessInfo() {
		PropertiesMap props = new PropertiesMap();
		props.put("operation.test.procInfo.dir", "${dir}");
		props.put("operation.test.procInfo.envVars.ENV_VAR_1", "${envVar1}");
		props.put("operation.test.procInfo.envVars.ENV_VAR_2", "${envVar2}");
		props.put("operation.test.procInfo.commands.1", "java -jar foo.jar -param=${param}");
		props.put("operation.test.procInfo.redirectProcessOutput", "${redirect}");
		props.put("operation.test.procInfo.logPrefix", "${logPrefix}");

		LtProcessModule module = new LtProcessModule(null, null, 0, 0);

		PlaceholderContainer pc = new DefaultPlaceholderContainer();
		pc.put("dir", "c:/somedir");
		pc.put("envVar1", "$JAVA_HOME/bin/java");
		pc.put("envVar2", "$ANT_HOME/bin/ant");
		pc.put("param", "foo");
		pc.put("redirect", "true");
		pc.put("logPrefix", "[myProc]");

		ProcessInfo processInfo = module.provideProcessInfo("test", props, pc);
		assertEquals(processInfo.getDirectory(), pc.get("dir"));
		assertThat(processInfo.getEnvVars(), hasEntry("ENV_VAR_1", pc.get("envVar1")));
		assertThat(processInfo.getEnvVars(), hasEntry("ENV_VAR_2", pc.get("envVar2")));
		assertThat(processInfo.getCommands(), contains("java -jar foo.jar -param=foo"));
		assertEquals(processInfo.isRedirectProcessOutput(), true);
		assertEquals(processInfo.getLogPrefix(), pc.get("logPrefix"));
	}
}
