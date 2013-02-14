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
package com.mgmtp.perfload.core.console.meta;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.lang3.time.FastDateFormat;
import org.testng.annotations.Test;

import com.mgmtp.perfload.core.common.config.Config;
import com.mgmtp.perfload.core.common.config.XmlConfigReader;

/**
 * @author rnaegele
 */
public class LtMetaInfoHandlerTest {
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

	@Test
	public void testMetaInfoForLoadProfileTest() throws Exception {
		XmlConfigReader reader = new XmlConfigReader("src/test/resources/testplan_loadprofile.xml", "UTF-8");
		long now = System.currentTimeMillis();
		Properties props = createMetaProperties(reader, now);
		String timestamp = DATE_FORMAT.format(now);

		assertEquals(props.getProperty("test.file"), "testplan_loadprofile.xml");
		assertEquals(props.getProperty("test.start"), timestamp);
		assertEquals(props.getProperty("test.finish"), timestamp);
		assertEquals(props.getProperty("daemon.1.host"), "localhost");
		assertEquals(props.getProperty("daemon.1.port"), "8042");
		assertEquals(props.getProperty("daemon.2.host"), "localhost");
		assertEquals(props.getProperty("daemon.2.port"), "8043");
		assertEquals(props.getProperty("testplan.testplan.targets"), "myTarget1,myTarget2");
		assertEquals(props.getProperty("testplan.testplan.operations"),
				"myOperation1,myOperation2,myOperation3,myOperation4,myOperation5,myOperation6");
		assertEquals(props.getProperty("plannedExecutions.myOperation1.myTarget1"), "1");
		assertEquals(props.getProperty("plannedExecutions.myOperation1.myTarget2"), "1");
		assertEquals(props.getProperty("plannedExecutions.myOperation2.myTarget1"), "2");
		assertEquals(props.getProperty("plannedExecutions.myOperation2.myTarget2"), "2");
		assertEquals(props.getProperty("plannedExecutions.myOperation3.myTarget1"), "1");
		assertEquals(props.getProperty("plannedExecutions.myOperation3.myTarget2"), "0");
		assertEquals(props.getProperty("plannedExecutions.myOperation4.myTarget1"), "3");
		assertEquals(props.getProperty("plannedExecutions.myOperation4.myTarget2"), "0");
		assertEquals(props.getProperty("plannedExecutions.myOperation5.myTarget1"), "0");
		assertEquals(props.getProperty("plannedExecutions.myOperation5.myTarget2"), "1");
		assertEquals(props.getProperty("plannedExecutions.myOperation6.myTarget1"), "0");
		assertEquals(props.getProperty("plannedExecutions.myOperation6.myTarget2"), "4");
	}

	private Properties createMetaProperties(final XmlConfigReader configReader, final long timestamp) throws Exception,
			IOException {
		Config config = configReader.readConfig();
		LtMetaInfoHandler handler = new LtMetaInfoHandler();
		LtMetaInfo metaInfo = handler.createMetaInformation(timestamp, timestamp, config);

		StringWriter sw = new StringWriter();
		handler.dumpMetaInfo(metaInfo, sw);

		StringReader sr = new StringReader(sw.toString());
		Properties props = new Properties();
		props.load(sr);
		return props;
	}
}
