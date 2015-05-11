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
package com.mgmtp.perfload.core.test.client;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.apache.commons.io.IOUtils.lineIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.LineIterator;

import com.google.inject.Provides;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class FibonacciModule extends AbstractWebLtModule {

	public FibonacciModule(final PropertiesMap testplanProperties) {
		super(testplanProperties);
	}

	@Override
	protected void doConfigureWebModule() {
		bindRequestFlowEventListener().to(FibonacciListener.class);
		install(new WebLtModule(testplanProperties));
	}

	@TestData
	@Provides
	Map<String, String> provideTestData() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try (InputStream is = cl.getResourceAsStream("testdata/fibonacci.txt")) {
			Map<String, String> result = newHashMapWithExpectedSize(20);
			for (LineIterator it = lineIterator(is, "UTF-8"); it.hasNext();) {
				String line = it.nextLine();
				if (line.startsWith("#")) {
					continue;
				}
				String[] columns = line.split(";");
				result.put(columns[0], columns[1]);
			}
			return result;
		} catch (IOException ex) {
			throw new IllegalStateException("Error reading test data.", ex);
		}
	}
}
