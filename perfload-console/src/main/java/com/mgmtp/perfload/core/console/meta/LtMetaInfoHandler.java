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
package com.mgmtp.perfload.core.console.meta;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.io.FileUtils.toFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ListMultimap;
import com.mgmtp.perfload.core.common.config.LoadProfileEvent;
import com.mgmtp.perfload.core.common.config.TestConfig;
import com.mgmtp.perfload.core.common.config.TestplanConfig;
import com.mgmtp.perfload.core.console.meta.LtMetaInfo.Executions;
import com.mgmtp.perfload.core.console.model.Daemon;

/**
 * Creates meta information on a test and dumps it to a file.
 *
 * @author rnaegele
 */
public class LtMetaInfoHandler {
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Creates meta information on a test.
	 *
	 * @param startTimestamp
	 *            timestamp taken at test finish
	 * @param finishTimestamp
	 *            timestamp taken at test start
	 * @param config
	 *            the {@link com.mgmtp.perfload.core.common.config.TestplanConfig} instance
	 * @return the meta information object
	 */
	public LtMetaInfo createMetaInformation(final ZonedDateTime startTimestamp, final ZonedDateTime finishTimestamp, final TestplanConfig config,
			final List<Daemon> daemons) {
		LtMetaInfo metaInfo = new LtMetaInfo();
		metaInfo.setStartTimestamp(startTimestamp);
		metaInfo.setFinishTimestamp(finishTimestamp);
		metaInfo.setTestplanFileName(config.getTestplanFile().getName());
		metaInfo.addDaemons(daemons);

		ListMultimap<String, String> operationsByTargets = ArrayListMultimap.create();
		for (TestConfig testConfig : config.getTestConfigs().values()) {
			for (LoadProfileEvent event : testConfig.getLoadProfileEvents()) {
				operationsByTargets.put(event.getTarget(), event.getOperation());
			}
		}

		Set<String> uniqueOperations = newHashSet(operationsByTargets.values());
		metaInfo.setLoadProfileTestInfo(operationsByTargets.keySet(), uniqueOperations);

		for (Entry<String, Collection<String>> entry : operationsByTargets.asMap().entrySet()) {
			String target = entry.getKey();
			Collection<String> operations = entry.getValue();
			for (final String operation : uniqueOperations) {
				int executions = Collections2.filter(operations, input -> input.equals(operation)).size();
				metaInfo.addExecutions(operation, target, executions);
			}
		}
		return metaInfo;
	}

	/**
	 * Dumps the specified meta information to specified writer.
	 *
	 * @param metaInfo
	 *            the meta information object
	 * @param writer
	 *            the writer
	 */
	public void dumpMetaInfo(final LtMetaInfo metaInfo, final Writer writer) {
		PrintWriter pr = new PrintWriter(writer);

		URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
		if (url.getPath().endsWith(".jar")) {
			try {
				JarFile jarFile = new JarFile(toFile(url));
				Manifest mf = jarFile.getManifest();
				Attributes attr = mf.getMainAttributes();
				pr.printf("perfload.implementation.version=%s", attr.getValue("Implementation-Version"));
				pr.println();
				pr.printf("perfload.implementation.date=%s", attr.getValue("Implementation-Date"));
				pr.println();
				pr.printf("perfload.implementation.revision=%s", attr.getValue("Implementation-Revision"));
				pr.println();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}

		pr.printf("test.file=%s", metaInfo.getTestplanFileName());
		pr.println();

		pr.printf("test.start=%s", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(metaInfo.getStartTimestamp()));
		pr.println();
		pr.printf("test.finish=%s", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(metaInfo.getFinishTimestamp()));
		pr.println();

		List<Daemon> daemonList = metaInfo.getDaemons();
		Collections.sort(daemonList);

		for (Daemon daemon : daemonList) {
			pr.printf("daemon.%d=%s:%d", daemon.getId(), daemon.getHost(), daemon.getPort());
			pr.println();
		}

		List<String> lpTargets = metaInfo.getLpTargets();
		if (!lpTargets.isEmpty()) {
			Collections.sort(lpTargets);
			pr.printf("targets=%s", on(',').join(lpTargets));
			pr.println();
		}

		List<String> lpOperations = metaInfo.getLpOperations();
		if (!lpOperations.isEmpty()) {
			Collections.sort(lpOperations);
			pr.printf("operations=%s", on(',').join(lpOperations));
			pr.println();
		}

		List<Executions> executionsList = metaInfo.getExecutionsList();
		Collections.sort(executionsList);
		for (Executions executions : executionsList) {
			pr.printf("executions.%s.%s=%d", executions.operation, executions.target, executions.executions);
			pr.println();
		}
	}
}
