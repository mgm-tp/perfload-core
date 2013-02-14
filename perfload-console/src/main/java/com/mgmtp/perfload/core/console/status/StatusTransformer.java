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
package com.mgmtp.perfload.core.console.status;

import java.util.Deque;
import java.util.Map;

import com.mgmtp.perfload.core.common.util.StatusInfo;

/**
 * Interface for transforming load test status information. Implementers may e. g. write the status to a file or display it in a
 * GUI.
 * 
 * @author rnaegele
 */
public abstract class StatusTransformer {

	protected final int totalThreadCount;

	public StatusTransformer(final int totalThreadCount) {
		this.totalThreadCount = totalThreadCount;
	}

	/**
	 * Performs some transformation logic on the given params.
	 * 
	 * @param statusInfoMap
	 *            the statusInfoMap
	 * @param threadActivitiesMap
	 *            the threadActivitiesMap
	 */
	public abstract void execute(final Map<StatusInfoKey, StatusInfo> statusInfoMap,
	        final Map<StatusInfoKey, Deque<ThreadActivity>> threadActivitiesMap);

}
