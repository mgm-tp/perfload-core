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
package com.mgmtp.perfload.core.client.event;

import java.util.EventListener;

import com.mgmtp.perfload.core.client.LtProcess;

/**
 * Event listener that can be registered with {@link LtProcess}.
 * 
 * @author rnaegele
 */
public interface LtProcessEventListener extends EventListener {

	/**
	 * Call when a test process starts.
	 * 
	 * @param event
	 *            the event
	 */
	void processStarted(LtProcessEvent event);

	/**
	 * Called when a test process is finshied.
	 * 
	 * @param event
	 *            the event
	 */
	void processFinished(LtProcessEvent event);
}
