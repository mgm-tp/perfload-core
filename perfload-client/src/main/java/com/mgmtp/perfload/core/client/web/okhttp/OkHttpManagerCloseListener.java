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
package com.mgmtp.perfload.core.client.web.okhttp;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;

/**
 * Event listener for closing the {@link OkHttpManager}. After each run, the current thread's OkHttp
 * client is nulled out and open connections are closed.
 *
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public final class OkHttpManagerCloseListener implements LtRunnerEventListener {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Provider<OkHttpManager> okHttpManagerProvider;

	/**
	 * @param okHttpManagerProvider
	 *            the {@link OkHttpManager} provider
	 */
	@Inject
	public OkHttpManagerCloseListener(final Provider<OkHttpManager> okHttpManagerProvider) {
		this.okHttpManagerProvider = okHttpManagerProvider;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void runStarted(final LtRunnerEvent event) {
		// no-op
	}

	/**
	 * Calls {@link OkHttpManager#close()}.
	 */
	@Override
	public void runFinished(final LtRunnerEvent event) {
		try {
			log.info("Closing OkHttpManager...");
			okHttpManagerProvider.get().close();
		} catch (Exception ex) {
			log.error("Error closing OkHttpManager", ex);
		}
	}
}
