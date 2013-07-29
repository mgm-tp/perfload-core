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

import static com.google.common.io.Files.toByteArray;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fest.assertions.api.GUAVA;
import org.testng.annotations.Test;

import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class XmlConfigReaderTest {

	@Test
	public void testLoadProfileConfig() throws Exception {
		XmlConfigReader confReader = new XmlConfigReader(new File("src/test/resources"), "testplan_loadprofile.xml");
		TestplanConfig config = confReader.readConfig();

		assertThat(config.getTotalProcessCount()).isEqualTo(34);
		assertThat(config.getTotalThreadCount()).isEqualTo(44);
		assertThat(config.getStartTimeOfLastEvent()).isEqualTo(42);

		List<TestJar> testJars = config.getTestJars();
		assertThat(testJars).hasSize(3);

		for (int j = 0; j < testJars.size(); ++j) {
			TestJar actual = testJars.get(j);
			byte[] content = toByteArray(new File("src/test/resources/test-lib/test" + (j + 1) + ".jar"));
			TestJar expected = new TestJar("test" + (j + 1) + ".jar", content);
			assertThat(actual).isEqualTo(expected);
		}

		List<ProcessConfig> processConfigs = config.getProcessConfigs();
		for (ProcessConfig processConfig : processConfigs) {
			assertThat(processConfig.getJvmArgs()).containsExactly("-Dsysprop1=value1", "-Dsysprop2=value2");
		}

		Map<ProcessKey, TestConfig> testConfigs = config.getTestConfigs();
		for (Entry<ProcessKey, TestConfig> entry : testConfigs.entrySet()) {
			TestConfig testConfig = entry.getValue();
			assertThat(testConfig.getGuiceModule()).isEqualTo("com.mgmtp.perfload.core.DummyModule");

			PropertiesMap properties = testConfig.getProperties();
			assertThat(properties).hasSize(2);
			assertThat(properties).contains(entry("prop1", "value1"));
			assertThat(properties).contains(entry("prop2", "value2"));
		}

		GUAVA.assertThat(config.getLoadProfileEventsByProcess()).hasSize(44);
		List<LoadProfileEvent> list = config.getLoadProfileEventsByProcess().get(new ProcessKey(1, 1));
		assertThat(list).hasSize(2);

		LoadProfileEvent event = list.get(0);
		assertThat(event.getStartTime()).isEqualTo(1);
		assertThat(event.getOperation()).isEqualTo("operation1");
		assertThat(event.getTarget()).isEqualTo("target");
		assertThat(event.getDaemonId()).isEqualTo(1);
		assertThat(event.getProcessId()).isEqualTo(1);
	}
}
