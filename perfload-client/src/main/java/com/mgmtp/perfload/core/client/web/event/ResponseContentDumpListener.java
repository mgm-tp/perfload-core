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
package com.mgmtp.perfload.core.client.web.event;

import static org.apache.commons.lang3.StringUtils.leftPad;

import java.io.File;
import java.io.IOException;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.mgmtp.perfload.core.client.config.scope.ThreadScoped;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;

/**
 * Listener for dumping response bodies to be used for debugging purposes only during driver
 * development.
 * 
 * @author rnaegele
 */
@ThreadScoped
@ThreadSafe
@Immutable
public final class ResponseContentDumpListener implements RequestFlowEventListener {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final MutableInt counter = new MutableInt();

	/**
	 * Does nothing.
	 */
	@Override
	public void beforeRequestFlow(final RequestFlowEvent event) {
		/* no-op */
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void afterRequestFlow(final RequestFlowEvent event) {
		/* no-op */
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void beforeRequest(final RequestFlowEvent event) {
		/* no-op */
	}

	/**
	 * Writes response bodies to the folder {@code dump/<executionId>} in the current directory.
	 */
	@Override
	public void afterRequest(final RequestFlowEvent event) {
		ResponseInfo responseInfo = event.getResponseInfo();
		if (responseInfo != null) {
			try {
				File file = new File("dump/" + event.getResponseInfo().getExecutionId() + "/"
						+ leftPad(counter.toString(), 2, '0') + ".html");
				Files.createParentDirs(file);
				file.createNewFile();
				byte[] body = responseInfo.getBody();
				if (body != null) {
					Files.write(responseInfo.getBody(), file);
				}
				counter.increment();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}
}
