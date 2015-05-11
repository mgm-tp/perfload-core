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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging the contents of {@link InputStream}s. The streams are piped to the log in a {@link Runnable} instance
 * that is passed to an {@link ExecutorService}.
 * 
 * @author rnaegele
 */
public class StreamGobbler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ExecutorService execService;

	/**
	 * Creates a new instance.
	 * 
	 * @param execService
	 *            The {@link ExecutorService} used to pass {@link Runnable} instances to.
	 */
	public StreamGobbler(final ExecutorService execService) {
		this.execService = execService;
	}

	/**
	 * <p>
	 * Adds an {@link InputStream} whose contents are to be processes. The logic is wrapped into a {@link Runnable} instance that
	 * is submitted to the {@link ExecutorService} provided in the constructor.
	 * </p>
	 * <p>
	 * The specified input stream is processed line by line by a reader. The specified callback is executed for every line.
	 * </p>
	 * 
	 * @param is
	 *            the {@link InputStream}
	 * @param encoding
	 *            the encoding to read from the stream
	 * @param callback
	 *            the callback
	 * @return a {@link Future} representing the completion of the Runnable
	 */
	public Future<?> addStream(final InputStream is, final String encoding, final GobbleCallback callback) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					// Wrap into a Reader.
					BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
					String line = null;

					// Read lines and execute the callback
					while ((line = br.readLine()) != null) {
						callback.execute(line);
					}
				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		};
		return execService.submit(task);
	}
}
