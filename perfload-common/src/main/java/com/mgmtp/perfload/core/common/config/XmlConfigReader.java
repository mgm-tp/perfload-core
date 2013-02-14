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

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;

import ch.lambdaj.group.Group;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.mgmtp.perfload.core.common.xml.Dom4jReader;

/**
 * Reads the test configuration from an XML file. The XML file may use XIncludes. It is validated
 * against the schema {@code config.xsd}.
 * 
 * @author rnaegele
 */
public class XmlConfigReader {

	private static final String SCHEMA_RESOURCE = "perfload-config.xsd";
	private static final Pattern MARKER_PATTERN = Pattern.compile("\\d+;\\[\\[marker\\]\\];[^;]*;(left|right);");

	private final File testplanFile;
	private final String encoding;
	private final List<TestJar> jarCache = newArrayListWithExpectedSize(10);

	/**
	 * @param testplanFile
	 *            the path to the XML config file
	 */
	public XmlConfigReader(final String testplanFile, final String encoding) {
		this.testplanFile = new File(testplanFile);
		this.encoding = encoding;
	}

	/**
	 * Reads the contents of the XML file into a {@link Config} object.
	 * 
	 * @return the Config object
	 */
	public Config readConfig() throws Exception {
		Config config = new Config();
		config.setTestplanFileName(testplanFile.getName());

		Element root = loadDocument().getRootElement();

		Element testplans = root.element("testplans");
		@SuppressWarnings("unchecked")
		List<Element> testplanElems = testplans.elements();
		Map<String, Element> testplanElemsMap = newHashMapWithExpectedSize(testplanElems.size());
		for (Element testplanElem : testplanElems) {
			testplanElemsMap.put(testplanElem.attributeValue("id"), testplanElem);
		}

		Element loadProfileElem = Iterables.get(testplanElems, 0).element("loadProfileConfig");
		checkState(loadProfileElem != null && testplanElems.size() == 1, "Load profile tests can only have one testplan configuration.");

		ListMultimap<Integer, LoadProfileEvent> loadProfileEvents = readLoadProfileEvents(loadProfileElem.getTextTrim());

		Element daemons = root.element("daemons");
		@SuppressWarnings("unchecked")
		List<Element> daemonElems = daemons.elements();

		int lpDaemonCount = loadProfileEvents.keySet().size();
		int tpDaemonCount = daemonElems.size();
		// The load profile must not use more daemons than are configured
		checkState(lpDaemonCount <= tpDaemonCount, String.format(
				"The selected load profile uses %d daemons while only %d are configured in the testplan.", lpDaemonCount, tpDaemonCount));

		for (Element daemonElem : daemonElems) {
			DaemonConfig daemonConfig = readDaemonConfig(daemonElem);
			Element testplanElem = Iterables.getOnlyElement(testplanElems);
			updateDaemonConfig(daemonConfig, testplanElem, loadProfileEvents.get(daemonConfig.getId()));
			config.addDaemonConfig(daemonConfig);
		}

		return config;
	}

	// Loads the DOM document
	private Document loadDocument() throws Exception {
		String schemaUrl = Thread.currentThread().getContextClassLoader().getResource(SCHEMA_RESOURCE).toString();
		String xmlFileUrl = testplanFile.toURI().toString();
		return Dom4jReader.loadDocument(new InputSource(xmlFileUrl), new StreamSource(schemaUrl), encoding);
	}

	// with load profile
	private void updateDaemonConfig(final DaemonConfig daemonConfig, final Element testplanElem,
			final List<LoadProfileEvent> loadProfileEvents) throws IOException {
		String testplanId = testplanElem.attributeValue("id");
		String module = testplanElem.attributeValue("module");

		// Group by processId
		Group<LoadProfileEvent> group = with(loadProfileEvents)
				.clone()
				.group(by(on(LoadProfileEvent.class).getProcessId()));

		// Iterate through groups and add configs to daemon config
		for (Group<LoadProfileEvent> subgroup : group.subgroups()) {
			// the key of the subgroup is the processId, which is what we had grouped by
			int processId = (Integer) subgroup.key();
			// the group contains all events for this processId
			List<LoadProfileEvent> eventsPerProcess = subgroup.findAll();

			// Create new load profile config for every process
			LoadProfileConfig lpc = new LoadProfileConfig(testplanId, module, eventsPerProcess);
			addProperties(testplanElem, lpc);
			daemonConfig.addTestplanConfig(processId, lpc);
			addProcessConfig(testplanElem, daemonConfig, processId);
		}

		addTestJars(testplanElem, daemonConfig);
	}

	private DaemonConfig readDaemonConfig(final Element elem) {
		int id = Integer.parseInt(elem.attributeValue("id"));
		String host = elem.attributeValue("host");
		int port = Integer.parseInt(elem.attributeValue("port"));
		return new DaemonConfig(id, host, port, jarCache);
	}

	private ListMultimap<Integer, LoadProfileEvent> readLoadProfileEvents(final String loadCurveEventsFile) throws IOException {
		ListMultimap<Integer, LoadProfileEvent> result = ArrayListMultimap.create();
		// relative to testplan
		File loadProfileConfigFile = new File(new File(testplanFile.getParentFile(), "loadprofiles"), loadCurveEventsFile);
		BufferedReader br = null;

		try {
			StrTokenizer st = StrTokenizer.getCSVInstance();
			st.setDelimiterChar(';');

			br = new BufferedReader(new InputStreamReader(new FileInputStream(loadProfileConfigFile), "UTF-8"));
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

				result.put(daemonId, new LoadProfileEvent(startTime, operation, target, daemonId, processId));
			}

			return result;
		} finally {
			IOUtils.closeQuietly(br);
		}
	}

	private void addTestJars(final Element elem, final DaemonConfig daemonConfig) throws IOException {
		@SuppressWarnings("unchecked")
		List<Element> jarElems = elem.element("testJars").elements();
		for (Element jarElem : jarElems) {
			String fileName = jarElem.attributeValue("name");
			String fileLocation = jarElem.attributeValue("dir");
			File jarFile = new File(fileLocation, fileName);
			InputStream is = null;
			try {
				is = new FileInputStream(jarFile);
				byte[] jarBytes = IOUtils.toByteArray(is);
				daemonConfig.addTestJar(fileName, jarBytes);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
	}

	private void addProperties(final Element elem, final AbstractTestplanConfig testplanConfig) {
		Element element = elem.element("properties");
		if (element != null) {
			@SuppressWarnings("unchecked")
			List<Element> propsElems = element.elements();
			for (Element propsElem : propsElems) {
				String name = propsElem.attributeValue("name");
				String value = propsElem.getText();
				testplanConfig.putProperty(name, value);
			}
		}
	}

	private void addProcessConfig(final Element elem, final DaemonConfig daemonConfig, final int processId) {
		Element element = elem.element("jvmargs");
		List<String> vmargs = newArrayListWithExpectedSize(2);
		if (element != null) {
			@SuppressWarnings("unchecked")
			List<Element> propsElems = element.elements();
			for (Element propsElem : propsElems) {
				vmargs.add(propsElem.getTextTrim());
			}
		}
		daemonConfig.addProcessConfig(new ProcessConfig(processId, daemonConfig.getId(), vmargs));
	}
}
