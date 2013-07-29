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

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.io.Files.toByteArray;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.text.StrTokenizer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.mgmtp.perfload.core.common.xml.Dom4jReader;

/**
 * Reads the test configuration from an XML file. The XML file may use XIncludes. It is validated
 * against the schema {@code config.xsd}.
 * 
 * @author rnaegele
 */
public class XmlConfigReader {

	private static final String SCHEMA_RESOURCE = "perfload-testplan.xsd";
	private static final Pattern MARKER_PATTERN = Pattern.compile("\\d+;\\[\\[marker\\]\\];[^;]*;(left|right);");

	private final File baseDir;
	private final File testplansDir;
	private final File testplanFile;

	/**
	 * @param testplanFileName
	 *            the path to the XML config file
	 */
	public XmlConfigReader(final File baseDir, final String testplanFileName) {
		this.baseDir = baseDir;
		this.testplansDir = new File(baseDir, "testplans");
		this.testplanFile = new File(testplansDir, testplanFileName);
	}

	/**
	 * Reads the contents of the XML file into a {@link TestplanConfig} object.
	 * 
	 * @return the Config object
	 */
	public TestplanConfig readConfig() throws Exception {
		Element testplan = loadDocument().getRootElement();

		ListMultimap<ProcessKey, LoadProfileEvent> loadProfileEvents = readLoadProfileEvents(testplan);
		List<TestJar> testJars = readTestJars(testplan);

		int totalProcessCount = loadProfileEvents.keySet().size();
		int totalThreadCount = loadProfileEvents.size();

		String guiceModule = testplan.elementTextTrim("module");
		PropertiesMap properties = readProperties(testplan);

		Map<ProcessKey, TestConfig> testConfigs = newHashMapWithExpectedSize(totalProcessCount);
		for (Entry<ProcessKey, Collection<LoadProfileEvent>> entry : loadProfileEvents.asMap().entrySet()) {
			ProcessKey key = entry.getKey();
			Collection<LoadProfileEvent> events = entry.getValue();
			testConfigs.put(key, new TestConfig(key.getProcessId(), guiceModule, properties, events));
		}

		long startTimeOfLastEvent = 0;
		for (LoadProfileEvent loadProfileEvent : loadProfileEvents.values()) {
			startTimeOfLastEvent = Math.max(startTimeOfLastEvent, loadProfileEvent.getStartTime());
		}

		List<String> jvmArgs = readJvmArgs(testplan);

		List<ProcessConfig> processConfigs = newArrayListWithCapacity(loadProfileEvents.keySet().size());
		for (ProcessKey key : loadProfileEvents.keySet()) {
			processConfigs.add(new ProcessConfig(key.getProcessId(), key.getDaemonId(), jvmArgs));
		}

		return new TestplanConfig(testplanFile, loadProfileEvents, testJars, testConfigs, processConfigs, totalProcessCount,
				totalThreadCount, startTimeOfLastEvent);
	}

	// Loads the DOM document
	private Document loadDocument() throws Exception {
		String schemaUrl = Thread.currentThread().getContextClassLoader().getResource(SCHEMA_RESOURCE).toString();
		String xmlFileUrl = testplanFile.toURI().toString();
		return Dom4jReader.loadDocument(new InputSource(xmlFileUrl), new StreamSource(schemaUrl), Charsets.UTF_8.name());
	}

	private ListMultimap<ProcessKey, LoadProfileEvent> readLoadProfileEvents(final Element testplan) throws IOException {
		ListMultimap<ProcessKey, LoadProfileEvent> eventsByProcess = ArrayListMultimap.create();
		String loadProfile = testplan.elementTextTrim("loadProfile");

		// relative to testplan
		File loadProfileConfigFile = new File(new File(testplanFile.getParentFile(), "loadprofiles"), loadProfile);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(loadProfileConfigFile), "UTF-8"))) {
			StrTokenizer st = StrTokenizer.getCSVInstance();
			st.setDelimiterChar(';');

			for (String line = null; (line = br.readLine()) != null;) {
				// ignore line that are blank, commented out, or represent markers
				if (isBlank(line) || startsWith(line, "#") || MARKER_PATTERN.matcher(line).matches()) {
					continue;
				}

				st.reset(line);
				String[] tokens = st.getTokenArray();

				long startTime = Long.parseLong(tokens[0]);
				String operation = tokens[1];
				String target = tokens[2];
				int daemonId = Integer.parseInt(tokens[3]);
				int processId = Integer.parseInt(tokens[4]);

				eventsByProcess.put(new ProcessKey(daemonId, processId), new LoadProfileEvent(startTime, operation, target,
						daemonId, processId));
			}
		}

		return eventsByProcess;
	}

	private List<TestJar> readTestJars(final Element testplan) throws IOException {
		File jarDir = new File(baseDir, "test-lib");
		@SuppressWarnings("unchecked")
		List<Element> jarElems = testplan.element("testJars").elements();
		List<TestJar> result = newArrayListWithCapacity(jarElems.size());
		for (Element jarElem : jarElems) {
			String fileName = jarElem.getTextTrim();
			byte[] jarBytes = toByteArray(new File(jarDir, fileName));
			result.add(new TestJar(fileName, jarBytes));
		}
		return ImmutableList.copyOf(result);
	}

	private PropertiesMap readProperties(final Element testplan) {
		PropertiesMap properties = new PropertiesMap();
		Element element = testplan.element("properties");
		if (element != null) {
			@SuppressWarnings("unchecked")
			List<Element> propsElems = element.elements();
			for (Element propsElem : propsElems) {
				String name = propsElem.attributeValue("name");
				String value = propsElem.getText();
				properties.put(name, value);
			}
		}
		return properties;
	}

	private List<String> readJvmArgs(final Element testplan) {
		List<String> result = newArrayListWithExpectedSize(2);
		Element element = testplan.element("jvmargs");
		if (element != null) {
			@SuppressWarnings("unchecked")
			List<Element> propsElems = element.elements();
			for (Element propsElem : propsElems) {
				result.add(propsElem.getTextTrim());
			}
		}
		return ImmutableList.copyOf(result);
	}
}
