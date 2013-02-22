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
package com.mgmtp.perfload.core.web.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.inject.Provider;

import org.mockito.internal.verification.VerificationModeFactory;
import org.testng.annotations.Test;

import com.mgmtp.perfload.core.web.event.HttpClientManagementListener;

/**
 * @author rnaegele
 */
public class HttpClientManagementListenerTest {

	@Test
	public void testListener() {
		final HttpClientManager hcm = mock(HttpClientManager.class);

		HttpClientManagementListener listener = new HttpClientManagementListener(new Provider<HttpClientManager>() {
			@Override
			public HttpClientManager get() {
				return hcm;
			}
		});

		listener.runStarted(null);
		listener.runFinished(null);

		// Must have been called exactly once.
		verify(hcm, VerificationModeFactory.times(1)).shutdown();
	}
}