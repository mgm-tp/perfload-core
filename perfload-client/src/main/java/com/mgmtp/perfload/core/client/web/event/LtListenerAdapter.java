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
package com.mgmtp.perfload.core.client.web.event;

import com.mgmtp.perfload.core.client.event.LtProcessEvent;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;

/**
 * Adapter class with no-op implementations for perfLoad's event listeners.
 * 
 * @author rnaegele
 */
public class LtListenerAdapter implements LtProcessEventListener, LtRunnerEventListener, RequestFlowEventListener {

	// LtProcessEventListener

	@Override
	public void processStarted(final LtProcessEvent event) {
		// no-op
	}

	@Override
	public void processFinished(final LtProcessEvent event) {
		// no-op
	}

	// LtRunnerEventListener

	@Override
	public void runStarted(final LtRunnerEvent event) {
		// no-op
	}

	@Override
	public void runFinished(final LtRunnerEvent event) {
		// no-op
	}

	// RequestFlowEventListener

	@Override
	public void beforeRequestFlow(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void afterRequestFlow(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void beforeRequest(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void afterRequest(final RequestFlowEvent event) {
		// no-op
	}
}
