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

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class XmlConfigReaderTest {

	@Test
	public void testLoadProfileConfig() throws Exception {
		XmlConfigReader confReader = new XmlConfigReader("src/test/resources/testplan_loadprofile.xml", "UTF-8");

		Config config = confReader.readConfig();
		assertEquals(config.getTotalProcessCount(), 34);
		assertEquals(config.getLastProfileEventStartTime(), 42);

		List<DaemonConfig> daemonConfigs = config.getDaemonConfigs();
		int size = daemonConfigs.size();
		assertEquals(size, 10);

		int port = 4242;

		for (int i = 0; i < size; ++i) {
			DaemonConfig daemonConfig = daemonConfigs.get(i);
			assertEquals(daemonConfig.getId(), i + 1);
			assertEquals(daemonConfig.getHost(), "localhost");
			assertEquals(daemonConfig.getPort(), port++);

			Collection<ProcessConfig> processConfigs = daemonConfig.getProcessConfigs();
			int procConfCount = processConfigs.size();
			switch (i) {
				case 0:
					assertEquals(procConfCount, 4);
					break;
				case 1:
					assertEquals(procConfCount, 6);
					break;
				case 2:
					assertEquals(procConfCount, 3);
					break;
				default:
					//
			}

			List<TestJar> testJars = daemonConfig.getTestJars();
			assertEquals(testJars.size(), 3);
			for (int j = 0; j < testJars.size(); ++j) {
				FileInputStream is = new FileInputStream("src/test/resources/test" + (j + 1) + ".jar");
				try {
					byte[] content = toByteArray(is);
					TestJar testJar = testJars.get(j);
					assertEquals(testJar.getName(), "test" + (j + 1) + ".jar");
					assertEquals(content, testJar.getContent());

					TestJar expected = new TestJar(testJar.getName(), content);
					assertEquals(testJar, expected);
					assertEquals(testJar.hashCode(), expected.hashCode());
				} finally {
					closeQuietly(is);
				}
			}

			for (int j = 0; j < procConfCount; ++j) {
				ProcessConfig procConf = get(processConfigs, j);
				assertEquals(procConf.getDaemonId(), i + 1);

				List<String> jvmArgs = procConf.getJvmArgs();
				assertEquals(jvmArgs.size(), 2);
				for (int k = 0; k < jvmArgs.size(); ++k) {
					assertEquals(jvmArgs.get(k), "-Dsysprop" + (k + 1) + "=value" + (k + 1));
				}

				AbstractTestplanConfig testplanConfig = daemonConfig.getTestplanConfig(procConf.getProcessId());
				assertThat(testplanConfig, is(instanceOf(LoadProfileConfig.class)));

				LoadProfileConfig lpc = (LoadProfileConfig) testplanConfig;
				assertEquals(lpc.getTestplanId(), "test");
				assertEquals(lpc.getGuiceModule(), "com.mgmtp.perfload.core.DummyModule");
				assertEquals(lpc.getLoadProfileEvents().size(), i < 2 ? 2 : 1);

				Map<String, String> properties = lpc.getProperties();
				assertEquals(properties.size(), 2);
				assertThat(properties, hasEntry("prop1", "value1"));
				assertThat(properties, hasEntry("prop2", "value2"));

				if (i > 1) {
					LoadProfileEvent lpe = getOnlyElement(lpc.getLoadProfileEvents());
					assertEquals(lpe.getDaemonId(), i + 1);
					assertEquals(lpe.getProcessId(), j + 1);

					if (j == 0) {
						assertTrue(lpe.getOperation().endsWith("1"));
					} else {
						assertTrue(lpe.getOperation().endsWith(String.valueOf(i + 1)));
					}
				}
			}
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testInvalidLoadProfileConfig() throws Exception {
		XmlConfigReader confReader = new XmlConfigReader("src/test/resources/testplan_invalid_loadprofile.xml", "UTF-8");
		confReader.readConfig();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testLoadProfileConfig2Plans() throws Exception {
		XmlConfigReader confReader = new XmlConfigReader("src/test/resources/testplan_loadprofile_2-plans.xml", "UTF-8");
		confReader.readConfig();
	}
}
