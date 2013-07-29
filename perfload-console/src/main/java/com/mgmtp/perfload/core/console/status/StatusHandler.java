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
package com.mgmtp.perfload.core.console.status;

import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Calendar;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.MapMaker;
import com.mgmtp.perfload.core.common.util.StatusInfo;
import com.mgmtp.perfload.core.common.util.StatusInfoType;

/**
 * {@link Runnable} implementation that takes {@link StatusInfo} objects from a queue transforming
 * them to a map of {@link StatusInfo} objects.
 * 
 * @author rnaegele
 */
public class StatusHandler {
	private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ConcurrentMap<StatusInfoKey, StatusInfo> statusInfoMap = new MapMaker().makeMap();
	private final ConcurrentMap<StatusInfoKey, Deque<ThreadActivity>> threadActivities = new MapMaker().makeMap();

	/**
	 * 
	 * @param statusInfo
	 *            The status info object
	 */
	public void addStatusInfo(final StatusInfo statusInfo) {
		StatusInfoKey key = new StatusInfoKey(statusInfo.getDaemonId(), statusInfo.getProcessId(), statusInfo.getThreadId());
		StatusInfo si = statusInfoMap.get(key);

		if (si == null && statusInfo.getType() != StatusInfoType.PROCESS_FINISHED) {
			log.debug("Adding status info: {}", statusInfo);
			si = statusInfoMap.putIfAbsent(key, statusInfo);
			if (si != null) {
				// Another thread was faster and we already have an entry for this key.
				// So we recursively call this method to update the existing entry.
				log.debug("Recursively updating status info: {}", statusInfo);
				addStatusInfo(statusInfo);
			} else {
				addThreadActivity(statusInfo.getDaemonId(), statusInfo.getProcessId(), statusInfo.getActiveThreads());
			}
		} else {
			log.debug("Updating status info {}:" + statusInfo);

			if (statusInfo.getType() == StatusInfoType.PROCESS_FINISHED) {
				final Integer daemonId = statusInfo.getDaemonId();
				final Integer processId = statusInfo.getProcessId();

				// Filter by daemonId and processId to get map of all thread entries for those
				Map<StatusInfoKey, StatusInfo> filteredMap = filterKeys(statusInfoMap, new Predicate<StatusInfoKey>() {
					@Override
					public boolean apply(final StatusInfoKey sik) {
						return sik.getDaemonId().equals(daemonId) && sik.getProcessId().equals(processId);
					}
				});

				// Create updates for filtered entries with final error status of the process
				Map<StatusInfoKey, StatusInfo> updates = newHashMap();
				for (Entry<StatusInfoKey, StatusInfo> entry : filteredMap.entrySet()) {
					StatusInfo siNew = new StatusInfo.Builder(entry.getValue())
							.stackTrace(statusInfo.getStackTrace())
							.error(statusInfo.getError())
							.build();
					updates.put(entry.getKey(), siNew);

					addThreadActivity(siNew.getDaemonId(), siNew.getProcessId(), 0);
				}

				statusInfoMap.putAll(updates);
			} else {
				// Create new builder based on existing status info object and update necessary fields
				StatusInfo.Builder builder = new StatusInfo.Builder(si)
						.stackTrace(statusInfo.getStackTrace())
						.finished(statusInfo.getFinished());

				si = builder.build();
				statusInfoMap.put(key, si);
			}
		}
	}

	private void addThreadActivity(final Integer daemonId, final Integer processId, final int activeThreads) {
		StatusInfoKey key = new StatusInfoKey(daemonId, processId, null);

		Deque<ThreadActivity> taDeque = threadActivities.get(key);
		if (taDeque == null) {
			Deque<ThreadActivity> newDeque = new LinkedBlockingDeque<>();
			taDeque = threadActivities.putIfAbsent(key, newDeque);
			if (taDeque == null) {
				taDeque = newDeque;
			}
		}
		String timestamp = TIMESTAMP_FORMAT.format(Calendar.getInstance());
		taDeque.offerLast(new ThreadActivity(daemonId, processId, activeThreads, timestamp));
	}

	public Map<StatusInfoKey, Deque<ThreadActivity>> getThreadActivitiesMap() {
		return Collections.unmodifiableMap(threadActivities);
	}

	/**
	 * Creates a snapshot of the status information collected so far. The status information is
	 * aggregated by daemon, process, and thread.
	 * 
	 * @return a list of {@link ThreadStatus} objects representing a snapshot of the current test
	 *         status
	 */
	public Map<StatusInfoKey, StatusInfo> getStatusInfoMap() {
		return Collections.unmodifiableMap(statusInfoMap);
	}
}
