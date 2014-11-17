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
package com.mgmtp.perfload.core.console.meta;

import com.google.common.collect.ImmutableList;
import com.mgmtp.perfload.core.common.config.TestplanConfig;
import com.mgmtp.perfload.core.common.config.XmlConfigReader;
import com.mgmtp.perfload.core.console.model.Daemon;
import org.apache.commons.lang3.time.FastDateFormat;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * @author rnaegele
 */
public class LtMetaInfoHandlerTest {
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

	@Test
	public void testMetaInfoForLoadProfileTest() throws Exception {
		XmlConfigReader reader = new XmlConfigReader(new File("src/test/resources"), "testplan_loadprofile.xml");
		ZonedDateTime now = ZonedDateTime.now();
		Properties props = createMetaProperties(reader, now);
		String timestamp = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(now);

		assertEquals(props.getProperty("test.file"), "testplan_loadprofile.xml");
		assertEquals(props.getProperty("test.start"), timestamp);
		assertEquals(props.getProperty("test.finish"), timestamp);
		assertEquals(props.getProperty("daemon.1"), "localhost:8042");
		assertEquals(props.getProperty("daemon.2"), "localhost:8043");
		assertEquals(props.getProperty("targets"), "myTarget1,myTarget2");
		assertEquals(props.getProperty("operations"), "myOperation1,myOperation2,myOperation3,myOperation4,myOperation5,myOperation6");
		assertEquals(props.getProperty("executions.myOperation1.myTarget1"), "1");
		assertEquals(props.getProperty("executions.myOperation1.myTarget2"), "1");
		assertEquals(props.getProperty("executions.myOperation2.myTarget1"), "2");
		assertEquals(props.getProperty("executions.myOperation2.myTarget2"), "2");
		assertEquals(props.getProperty("executions.myOperation3.myTarget1"), "1");
		assertEquals(props.getProperty("executions.myOperation3.myTarget2"), "0");
		assertEquals(props.getProperty("executions.myOperation4.myTarget1"), "3");
		assertEquals(props.getProperty("executions.myOperation4.myTarget2"), "0");
		assertEquals(props.getProperty("executions.myOperation5.myTarget1"), "0");
		assertEquals(props.getProperty("executions.myOperation5.myTarget2"), "1");
		assertEquals(props.getProperty("executions.myOperation6.myTarget1"), "0");
		assertEquals(props.getProperty("executions.myOperation6.myTarget2"), "4");
	}

	private Properties createMetaProperties(final XmlConfigReader configReader, final ZonedDateTime timestamp) throws Exception,
			IOException {
		TestplanConfig config = configReader.readConfig();
		LtMetaInfoHandler handler = new LtMetaInfoHandler();
		LtMetaInfo metaInfo = handler.createMetaInformation(timestamp, timestamp, config,
				ImmutableList.of(new Daemon(1, "localhost", 8042), new Daemon(2, "localhost", 8043)));

		StringWriter sw = new StringWriter();
		handler.dumpMetaInfo(metaInfo, sw);

		StringReader sr = new StringReader(sw.toString());
		Properties props = new Properties();
		props.load(sr);
		return props;
	}
}
