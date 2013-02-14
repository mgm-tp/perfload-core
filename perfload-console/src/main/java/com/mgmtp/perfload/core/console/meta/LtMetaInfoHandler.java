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

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.toFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.lambdaj.group.Group;

import com.mgmtp.perfload.core.common.config.AbstractTestplanConfig;
import com.mgmtp.perfload.core.common.config.Config;
import com.mgmtp.perfload.core.common.config.DaemonConfig;
import com.mgmtp.perfload.core.common.config.LoadProfileConfig;
import com.mgmtp.perfload.core.common.config.LoadProfileEvent;
import com.mgmtp.perfload.core.console.meta.LtMetaInfo.Daemon;
import com.mgmtp.perfload.core.console.meta.LtMetaInfo.PlannedExecutions;

/**
 * Creates meta information on a test and dumps it to a file.
 * 
 * @author rnaegele
 */
public class LtMetaInfoHandler {
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Creates meta information on a test.
	 * 
	 * @param finishTimestamp
	 *            timestamp taken at test start
	 * @param startTimestamp
	 *            timestamp taken at test finish
	 * @param config
	 *            the {@link Config} instance
	 * @return the meta information object
	 */
	public LtMetaInfo createMetaInformation(final long startTimestamp, final long finishTimestamp, final Config config) {
		LtMetaInfo metaInfo = new LtMetaInfo();
		metaInfo.setStartTimestamp(startTimestamp);
		metaInfo.setFinishTimestamp(finishTimestamp);
		metaInfo.setTestplanFile(config.getTestplanFileName());

		List<LoadProfileEvent> lpEvents = newArrayList();
		List<DaemonConfig> daemonConfigs = config.getDaemonConfigs();
		for (DaemonConfig dc : daemonConfigs) {
			metaInfo.addDaemon(dc.getId(), dc.getHost(), dc.getPort());

			Map<Integer, AbstractTestplanConfig> testplanConfigs = dc.getTestplanConfigs();
			for (AbstractTestplanConfig tpc : testplanConfigs.values()) {
				lpEvents.addAll(((LoadProfileConfig) tpc).getLoadProfileEvents());
				String testplanId = tpc.getTestplanId();
				metaInfo.setLpTestplanId(testplanId);
			}
		}

		// Group by targets
		Group<LoadProfileEvent> group = with(lpEvents).clone().group(by(on(LoadProfileEvent.class).getTarget()));
		// Get distinct targets
		Set<String> targets = group.keySet();

		// Group by operations and targets
		group = with(lpEvents).clone().group(by(on(LoadProfileEvent.class).getOperation()),
				by(on(LoadProfileEvent.class).getTarget()));
		// Get distinct operations
		Set<String> operations = group.keySet();

		metaInfo.setLoadProfileTestInfo(targets, operations);

		for (String operation : operations) {
			Group<LoadProfileEvent> opGroup = group.findGroup(operation);
			for (String target : targets) {
				// number of targets in an operations group is the number of executions
				int executions = opGroup.find(target).size();
				metaInfo.addPlannedExecutions(operation, target, executions);
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

		pr.printf("test.start=%s", DATE_FORMAT.format(metaInfo.getStartTimestamp()));
		pr.println();
		pr.printf("test.finish=%s", DATE_FORMAT.format(metaInfo.getFinishTimestamp()));
		pr.println();

		List<Daemon> daemonList = metaInfo.getDaemonList();
		Collections.sort(daemonList);

		for (Daemon daemon : daemonList) {
			pr.printf("daemon.%d.host=%s", daemon.id, daemon.host);
			pr.println();
			pr.printf("daemon.%d.port=%d", daemon.id, daemon.port);
			pr.println();
		}

		List<String> lpTargets = metaInfo.getLpTargets();
		if (!lpTargets.isEmpty()) {
			Collections.sort(lpTargets);
			pr.printf("testplan.%s.targets=%s", metaInfo.getLpTestplanId(), on(',').join(lpTargets));
			pr.println();
		}

		List<String> lpOperations = metaInfo.getLpOperations();
		if (!lpOperations.isEmpty()) {
			Collections.sort(lpOperations);
			pr.printf("testplan.%s.operations=%s", metaInfo.getLpTestplanId(), on(',').join(lpOperations));
			pr.println();
		}

		List<PlannedExecutions> plannedExecutionsList = metaInfo.getPlannedExecutionsList();
		Collections.sort(plannedExecutionsList);
		for (PlannedExecutions executions : plannedExecutionsList) {
			pr.printf("plannedExecutions.%s.%s=%d", executions.operation, executions.target, executions.executions);
			pr.println();
		}
	}
}
