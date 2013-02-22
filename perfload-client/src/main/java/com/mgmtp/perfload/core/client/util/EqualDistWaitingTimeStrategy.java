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
package com.mgmtp.perfload.core.client.util;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Equal-distributed waiting time strategy.
 * 
 * @author rnaegele
 */
public final class EqualDistWaitingTimeStrategy extends AbstractDistWaitingTimeStrategy {

	@Inject
	public EqualDistWaitingTimeStrategy(@Named("wtm.strategy.equaldist.intervalMinMillis") final long intervalMinMillis,
	        @Named("wtm.strategy.equaldist.intervalMaxMillis") final long intervalMaxMillis) {
		super(intervalMinMillis, intervalMaxMillis);
	}

	@Override
	public long calculateWaitingTime() {
		double probability = Math.random();
		return calculateNormedValue(probability);
	}
}