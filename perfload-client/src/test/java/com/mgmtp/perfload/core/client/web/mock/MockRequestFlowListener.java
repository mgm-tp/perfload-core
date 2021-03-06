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
package com.mgmtp.perfload.core.client.web.mock;

import com.mgmtp.perfload.core.client.web.event.RequestFlowEvent;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;

/**
 * @author rnaegele
 */
public class MockRequestFlowListener implements RequestFlowEventListener {

	private int eventCalls;

	@Override
	public void beforeRequest(final RequestFlowEvent event) {
		eventCalls++;
	}

	@Override
	public void beforeRequestFlow(final RequestFlowEvent event) {
		eventCalls++;
	}

	@Override
	public void afterRequest(final RequestFlowEvent event) {
		eventCalls++;
	}

	@Override
	public void afterRequestFlow(final RequestFlowEvent event) {
		eventCalls++;
	}

	public int getEventCalls() {
		return eventCalls;
	}
}