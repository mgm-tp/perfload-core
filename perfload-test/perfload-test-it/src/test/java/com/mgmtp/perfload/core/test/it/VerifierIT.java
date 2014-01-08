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
package com.mgmtp.perfload.core.test.it;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

/**
 * Base class of all verifier classes.<br>
 * It checks the number of log files created by the test clients and if the content contains the
 * correct number of ERROR and SUCCESSFUL log lines.
 * 
 * @author sschmid
 */
public class VerifierIT {

	private static final String CLIENT_DIR = "target/it/client";
	private static final Pattern ERROR_PATTERN = Pattern.compile("\"ERROR\"");
	private static final Pattern SUCCESSFUL_PATTERN = Pattern.compile("\"SUCCESS\"");
	private static final Pattern URI_PATTERN = Pattern.compile("\"[^\"]+/fibonacci[^\"]*\"");
	private static final Pattern URI_ALIAS_PATTERN = Pattern.compile("\"fibAlias\"");
	private static final Pattern LOG_FILE_PATTERN = Pattern.compile(".*_measuring.log");

	/**
	 * Checks if the number of patterns match to the expected ones.
	 * 
	 * @param pattern
	 *            The pattern to check
	 * @param expectedMatches
	 *            Number of expected matches
	 * @param content
	 *            The content where to look for the patterns
	 */
	protected void assertMatches(final Pattern pattern, final int expectedMatches, final String content) {
		Matcher matcher = pattern.matcher(content);
		int actualMatches = 0;
		while (matcher.find()) {
			actualMatches++;
		}
		assertEquals(actualMatches, expectedMatches, "Number of request " + pattern + " not correct.");
	}

	/**
	 * Get all log files the fit to the given pattern in the given directory
	 * 
	 * @param clientDir
	 *            The directory where to look for the files
	 * @param logFilePattern
	 *            The pattern the log filename must match
	 * @return All log files in the given directory that match to the given pattern.
	 */
	protected File[] getLogs(final File clientDir, final Pattern logFilePattern) {
		File[] logs = clientDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				Matcher matcher = logFilePattern.matcher(name);
				return matcher.find();
			}
		});

		return logs;

	}

	/**
	 * Verifies if the number of log files and the content of log files is as expected.
	 * 
	 * @throws IOException
	 *             If an I/O Error occurs while reading the log filess
	 */
	@Test
	public void verify() throws IOException {
		System.out.println("Verifying test results...");

		// Get all log files that belong to this test
		File[] logs = this.getLogs(new File(CLIENT_DIR), LOG_FILE_PATTERN);

		// Check if the number of log files equals the expected one.
		assertEquals(logs.length, 2, "Number of measuring logs not correct."); // one per process

		int expectedSuccessfulMatches = 6 * 20; // 6 executions per process, 20 requests in flow
		int expectedErrorMatches = 0;
		int expectedUriMatches = 6 * 20 + 6 * 10;
		int expectedUriAliasMatches = 6 * 10;

		// Loop over all log files
		for (File log : logs) {
			InputStream is = null;
			try {
				// Get log file content
				is = new FileInputStream(log);
				String content = IOUtils.toString(is);

				// Check log file content
				assertMatches(ERROR_PATTERN, expectedErrorMatches, content);
				assertMatches(SUCCESSFUL_PATTERN, expectedSuccessfulMatches, content);
				assertMatches(URI_PATTERN, expectedUriMatches, content);
				assertMatches(URI_ALIAS_PATTERN, expectedUriAliasMatches, content);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
	}
}
