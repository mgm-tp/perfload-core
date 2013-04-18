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
package com.mgmtp.perfload.core.client.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.inject.Provider;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.driver.LtDriver;
import com.mgmtp.perfload.core.client.web.WebLtDriver;
import com.mgmtp.perfload.core.client.web.flow.RequestFlowHandler;

/**
 * @author rnaegele
 */
public class WebLtDriverTest {

	@Test
	public void testWebLtDriver() throws Exception {
		final RequestFlowHandler handler = mock(RequestFlowHandler.class);

		Provider<RequestFlowHandler> provider = new Provider<RequestFlowHandler>() {
			@Override
			public RequestFlowHandler get() {
				return handler;
			}
		};
		LtDriver driver = new WebLtDriver(provider);
		driver.execute();

		verify(handler).execute();
	}
}
