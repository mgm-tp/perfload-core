/*
 * Copyright (c) 2002-2015 mgm technology partners GmbH
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class PropertiesMapTest {

	@Test
	public void testGetPut() {
		PropertiesMap pm = new PropertiesMap();

		String stringVal = "someString";
		Integer intVal = Integer.MAX_VALUE;
		Float floatVal = Float.MAX_VALUE;
		Double doubleVal = Double.MAX_VALUE;
		Long longVal = Long.MAX_VALUE;
		Boolean boolVal = Boolean.TRUE;

		pm.put("string", stringVal);
		pm.put("int", String.valueOf(intVal));
		pm.put("float", String.valueOf(floatVal));
		pm.put("double", String.valueOf(doubleVal));
		pm.put("long", String.valueOf(longVal));
		pm.put("bool", String.valueOf(boolVal));

		assertEquals(pm.get("string"), stringVal);
		assertEquals(pm.getInteger("int"), intVal);
		assertEquals(pm.getFloat("float"), floatVal);
		assertEquals(pm.getDouble("double"), doubleVal);
		assertEquals(pm.getLong("long"), longVal);
		assertEquals(pm.getBoolean("bool"), boolVal);
		assertFalse(pm.getBoolean("nonexistingKey"));
	}

	@Test
	public void testDefaults() {
		PropertiesMap defaults = new PropertiesMap();
		defaults.put("string1", "someString1");

		PropertiesMap pm = new PropertiesMap(defaults);
		pm.put("string2", "someString2");

		String stringVal1 = "someString1";
		String stringVal2 = "someString2";
		Integer intVal = Integer.MAX_VALUE;
		Float floatVal = Float.MAX_VALUE;
		Double doubleVal = Double.MAX_VALUE;
		Long longVal = Long.MAX_VALUE;
		Boolean boolVal = Boolean.TRUE;

		assertEquals(pm.get("string1"), stringVal1);
		assertEquals(pm.get("string2"), stringVal2);
		assertEquals(pm.getInteger("int"), null);
		assertEquals(pm.getFloat("float"), null);
		assertEquals(pm.getDouble("double"), null);
		assertEquals(pm.getLong("long"), null);
		assertEquals(pm.getBoolean("bool"), Boolean.FALSE);

		assertEquals(pm.get("string", "foo"), "foo");
		assertEquals(pm.getInteger("int", intVal), intVal);
		assertEquals(pm.getFloat("float", floatVal), floatVal);
		assertEquals(pm.getDouble("double", doubleVal), doubleVal);
		assertEquals(pm.getLong("long", longVal), longVal);
		assertEquals(pm.getBoolean("bool", boolVal), boolVal);
	}

	@Test
	public void testToAndFromProperties() throws IOException {
		Properties props = new Properties();
		props.put("string", "someString");

		PropertiesMap pm = PropertiesMap.fromProperties(props);

		assertEquals(pm.get("string"), "someString");

		assertEquals(pm.toProperties(), props);
		assertEquals(pm.toProperties(true), props);

		pm = new PropertiesMap();
		for (char ch = 'A'; ch <= 'Z'; ++ch) {
			pm.put(String.valueOf(ch) + String.valueOf(ch), "someValue");
		}

		StringWriter sw = new StringWriter();
		props = pm.toProperties(true);
		props.store(sw, "sorted props");
		String sortedProps = sw.toString();

		sw = new StringWriter();
		props = pm.toProperties(false);
		props.store(sw, "unsorted props");
		String unsortedProps = sw.toString();

		assertFalse(sortedProps.equals(unsortedProps));

		BufferedReader br = new BufferedReader(new StringReader(sortedProps));

		// Header lines
		br.readLine();
		br.readLine();

		for (char ch = 'A'; ch <= 'Z'; ++ch) {
			assertTrue(br.readLine().startsWith(String.valueOf(ch) + String.valueOf(ch)),"for character "+String.valueOf(ch));
		}
	}
}
