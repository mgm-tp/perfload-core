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
package com.mgmtp.perfload.core.client.util;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Waiting time strategy that introduces constant waiting times.
 * 
 * @author rnaegele
 */
public final class ConstantWaitingTimeStrategy implements WaitingTimeStrategy {

	private final long waitingTimeMillis;

	/**
	 * @param waitingTimeMillis
	 *            the time to wait
	 */
	@Inject
	public ConstantWaitingTimeStrategy(@Named("wtm.strategy.constant.waitingTimeMillis") final long waitingTimeMillis) {
		this.waitingTimeMillis = waitingTimeMillis;
	}

	@Override
	public long calculateWaitingTime() {
		return waitingTimeMillis;
	}
}
