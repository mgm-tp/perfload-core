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
package com.mgmtp.perfload.core.client.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.net.URL;

import org.testng.annotations.Test;

/**
 * Unit test for {@link LocalFirstClassLoader}
 * 
 * @author rnaegele
 */
public class LocalFirstClassLoaderTest {

	@Test
	public void testClassLoading() throws ClassNotFoundException {
		URL url = getClass().getResource("loader-test.jar");
		String className = "com.mgmtp.perfload.core.client.util.LtContext";

		LocalFirstClassLoader cl1 = new LocalFirstClassLoader(url);
		Class<?> loader1Class1 = cl1.loadClass(className);
		Class<?> loader1Class2 = cl1.loadClass(className);
		assertSame(loader1Class1, loader1Class2);

		LocalFirstClassLoader cl2 = new LocalFirstClassLoader(url);
		Class<?> loader2Class1 = cl2.loadClass(className);
		Class<?> loader2Class2 = cl2.loadClass(className);
		assertSame(loader2Class1, loader2Class2);

		assertNotSame(loader1Class1, loader2Class1);
		assertNotSame(loader1Class2, loader2Class2);
	}

	@Test
	public void testResourceLoading() {
		URL url = getClass().getResource("loader-test.jar");
		String className = "com/mgmtp/perfload/core/client/util/LtContext.class";

		LocalFirstClassLoader cl = new LocalFirstClassLoader(url);
		URL resourceUrl = cl.getResource(className);

		assertNotNull(resourceUrl);

		resourceUrl = cl.getResource("some-non-existing-resource");

		assertNull(resourceUrl);
	}

	@Test
	public void testToString() {
		URL url = getClass().getResource("loader-test.jar");
		LocalFirstClassLoader cl = new LocalFirstClassLoader(url);
		String actual = cl.toString();
		String expected = cl.getClass().getSimpleName() + "[urls={" + url + "}]";
		assertEquals(actual, expected);
	}
}
