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
package com.mgmtp.perfload.core.console.status;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.common.util.StatusInfo;

/**
 * {@link StatusTransformer} implementation that writes status information to a given file.
 * 
 * @author rnaegele
 */
public class FileStatusTransformer extends StatusTransformer {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final char DELIM_CHAR = '|';
	private static final String DELIMITER = " " + DELIM_CHAR + " ";
	private static final String LINE_START = DELIM_CHAR + " ";
	private static final String LINE_END = " " + DELIM_CHAR;

	private static final String DAEMON_ID = "DaemonId";
	private static final String PROCESS_ID = "ProcessId";
	private static final String THREAD_ID = "ThreadId";
	private static final String OPERATION = "Operation                ";
	private static final String TARGET = "Target                   ";
	private static final String FINISHED = "Finished";
	private static final String ERROR = "Error";
	private static final String RESULT = "Result ";

	private static final List<String> HEADER_LIST = asList(DAEMON_ID, PROCESS_ID, THREAD_ID, OPERATION, TARGET, FINISHED, ERROR, RESULT);
	private static final String HEADER = LINE_START + on(DELIMITER).join(HEADER_LIST) + LINE_END;

	private static final char[] HR;
	static {

		HR = new char[HEADER.length()];
		int index = 0;

		fill(HR, index, index + 1, '|');
		fill(HR, ++index, index + 1, '-');
		++index;

		boolean first = true;
		for (String s : HEADER_LIST) {
			if (!first) {
				fill(HR, index, index + 1, '-');
				fill(HR, ++index, index + 1, '|');
				fill(HR, ++index, index + 1, '-');
				++index;
			}
			first = false;

			fill(HR, index, s.length() + index, '-');
			index = s.length() + index;
		}

		fill(HR, index, index + 1, '-');
		fill(HR, ++index, index + 1, '|');
	}

	private final File statusFile;
	private final File threadActivityFile;
	private final String encoding;

	private int maxConcurrentThreads;

	/**
	 * @param statusFile
	 *            the file to write the status information to
	 * @param encoding
	 *            the file encoding
	 * @param totalThreadCount
	 *            the total number of threads executed during the test
	 */
	public FileStatusTransformer(final int totalThreadCount, final File statusFile, final File threadActivityFile,
			final String encoding) {
		super(totalThreadCount);
		this.statusFile = statusFile;
		this.threadActivityFile = threadActivityFile;
		this.encoding = encoding;
	}

	/**
	 * <p>
	 * Writes status information to the file. Each write replaces the complete contents of the file
	 * with the updated status information. The information is written in a fixed-width text format
	 * using the pipe symbol as separator.
	 * </p>
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * DaemonId | ProcessId | ThreadId | Operation   | Target   | Finished | Error | Result
	 *        1 |         1 |        1 | myOperation | myTarget | true     | false | SUCCESS
	 *        1 |         1 |        2 | myOperation | myTarget | true     | true  | SUCCESS
	 *        2 |         1 |        1 | myOperation | myTarget | false    | false |
	 *        3 |         1 |        1 | myOperation | myTarget | true     | true  | ERROR
	 * </pre>
	 * 
	 * </p>
	 */
	@Override
	public void execute(final Map<StatusInfoKey, StatusInfo> statusInfoMap,
			final Map<StatusInfoKey, Deque<ThreadActivity>> threadActivitiesMap) {
		processStatusInfo(statusInfoMap, threadActivitiesMap);
		processThreadActivity(threadActivitiesMap);
	}

	private void processStatusInfo(final Map<StatusInfoKey, StatusInfo> statusInfoMap,
			final Map<StatusInfoKey, Deque<ThreadActivity>> threadActivitiesMap) {
		PrintWriter pr = null;
		try {
			pr = new PrintWriter(statusFile, encoding);
			pr.println(HR);
			pr.println(HEADER);
			pr.println(HR);

			Set<StatusInfoKey> sortedKeys = newTreeSet(statusInfoMap.keySet());

			int threadsFinished = 0;
			for (StatusInfoKey key : sortedKeys) {
				StatusInfo statusInfo = statusInfoMap.get(key);

				if (statusInfo.getFinished() != null && statusInfo.getFinished()) {
					threadsFinished++;
				}

				StrBuilder sb = new StrBuilder(200);

				sb.append(LINE_START);
				sb.appendFixedWidthPadLeft(statusInfo.getDaemonId(), DAEMON_ID.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadLeft(statusInfo.getProcessId(), PROCESS_ID.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadLeft(statusInfo.getThreadId(), THREAD_ID.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadRight(statusInfo.getOperation(), OPERATION.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadRight(statusInfo.getTarget(), TARGET.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadRight(statusInfo.getFinished(), FINISHED.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadRight(statusInfo.getStackTrace() != null, ERROR.length(), ' ');
				sb.append(DELIMITER);
				sb.appendFixedWidthPadRight(statusInfo.getError() != null ? statusInfo.getError() ? "ERROR" : "SUCCESS" : "", RESULT.length(), ' ');
				sb.append(LINE_END);

				pr.println(sb);
			}

			pr.println(HR);

			StrBuilder sb = new StrBuilder(HR.length);
			sb.append(LINE_START);

			int activeThreads = 0;
			for (Deque<ThreadActivity> ta : threadActivitiesMap.values()) {
				activeThreads += ta.peekLast().getActiveThreads();
			}
			sb.appendFixedWidthPadRight("Currently active client threads: " + activeThreads, HR.length - LINE_START.length() - LINE_END.length(), ' ');
			sb.append(LINE_END);
			pr.println(sb);

			sb = new StrBuilder(HR.length);
			sb.append(LINE_START);
			maxConcurrentThreads = max(maxConcurrentThreads, activeThreads);
			sb.appendFixedWidthPadRight("Maximum concurrent client threads: "
					+ maxConcurrentThreads, HR.length - LINE_START.length() - LINE_END.length(), ' ');
			sb.append(LINE_END);
			pr.println(sb);

			sb = new StrBuilder(HR.length);
			sb.append(LINE_START);
			sb.appendFixedWidthPadRight("Total threads finished: "
					+ threadsFinished + "/" + totalThreadCount, HR.length - LINE_START.length() - LINE_END.length(), ' ');
			sb.append(LINE_END);
			pr.println(sb);

			pr.println(HR);
		} catch (IOException ex) {
			log.error("Error writing load test status to file: {}", statusFile, ex);
		} finally {
			IOUtils.closeQuietly(pr);
		}
	}

	private void processThreadActivity(final Map<StatusInfoKey, Deque<ThreadActivity>> threadActivitiesMap) {
		PrintWriter pr = null;
		try {
			pr = new PrintWriter(threadActivityFile, encoding);
			pr.println("timestamp;daemonId;processId;activeThreads");

			List<String> taLines = newArrayList();

			for (Deque<ThreadActivity> threadActivities : threadActivitiesMap.values()) {
				for (ThreadActivity ta : threadActivities) {
					taLines.add(String.format("%s;%d;%d;%d", ta.getTimestamp(), ta.getDaemonId(), ta.getProcessId(), ta.getActiveThreads()));
				}
			}

			Collections.sort(taLines);

			for (String line : taLines) {
				pr.println(line);
			}
		} catch (IOException ex) {
			log.error("Error writing thread activity to file: {}", threadActivityFile, ex);
		} finally {
			IOUtils.closeQuietly(pr);
		}
	}
}
