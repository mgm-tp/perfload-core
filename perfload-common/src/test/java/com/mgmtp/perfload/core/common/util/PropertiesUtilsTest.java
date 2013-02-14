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
package com.mgmtp.perfload.core.common.util;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class PropertiesUtilsTest {

	@Test
	public void testLoad() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.utf8.props");
		List<String> lines;
		try {
			lines = readLines(is, "UTF-8");
		} finally {
			closeQuietly(is);
		}

		PropertiesMap properties = PropertiesUtils.loadProperties("test.utf8.props", "UTF-8", false);

		for (String line : lines) {
			if (isNotBlank(line) && !line.startsWith("#")) {
				String[] propArray = line.split("=");
				assertEquals(propArray[1], properties.get(propArray[0]));
			}
		}
	}

	@Test
	public void testNonExistingProps() throws IOException {
		PropertiesMap properties = PropertiesUtils.loadProperties("blablubb", "UTF-8", false);
		assertTrue(properties.isEmpty());

		try {
			PropertiesUtils.loadProperties("blablubb", "UTF-8", true);
		} catch (IOException ex) {
			return;
		}

		fail("Expected IOException not thrown.");
	}

	@Test
	public void testGetSubCollection() throws IOException {
		PropertiesMap properties = PropertiesUtils.loadProperties("test.utf8.props", "UTF-8", false);

		List<String> list = PropertiesUtils.getSubList(properties, "some.prop.list");
		assertThat(list, contains("list1", "list2", "list3"));
		assertThat(list.size(), is(equalTo(3)));

		list = PropertiesUtils.getSubList(properties, "some.prop.list.");
		assertThat(list, contains("list1", "list2", "list3"));
		assertThat(list.size(), is(equalTo(3)));
	}

	@Test
	public void testGetSubMap() throws IOException {
		PropertiesMap properties = PropertiesUtils.loadProperties("test.utf8.props", "UTF-8", false);

		Map<String, String> map = PropertiesUtils.getSubMap(properties, "some.prop.map");
		assertThat(map, hasEntry("key1", "value1"));
		assertThat(map, hasEntry("key2", "value2"));
		assertThat(map, hasEntry("key3", "value3"));
		assertThat(map.size(), is(equalTo(3)));

		map = PropertiesUtils.getSubMap(properties, "some.prop.map.");
		assertThat(map, hasEntry("key1", "value1"));
		assertThat(map, hasEntry("key2", "value2"));
		assertThat(map, hasEntry("key3", "value3"));
		assertThat(map.size(), is(equalTo(3)));
	}
}
