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

import com.mgmtp.perfload.core.client.runner.LtRunner;

/**
 * Event listener that can be registered with {@link LtRunner}.
 * 
 * @author rnaegele
 */
public interface LtRunnerEventListener extends EventListener {

	/**
	 * Called before the test driver is executed.
	 * 
	 * @param event
	 *            the event
	 */
	void runStarted(LtRunnerEvent event);

	/**
	 * Called after the driver execution has completed.
	 * 
	 * @param event
	 *            the event
	 */
	void runFinished(LtRunnerEvent event);
}
