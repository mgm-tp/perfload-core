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

import JSci.maths.statistics.BetaDistribution;

/**
 * Beta-distributed waiting time strategy.
 * 
 * @author rnaegele
 */
public final class BetaDistWaitingTimeStrategy extends AbstractDistWaitingTimeStrategy {

	private final double betaDistParamA;
	private final double betaDistParamB;

	@Inject
	public BetaDistWaitingTimeStrategy(@Named("wtm.strategy.betadist.intervalMinMillis") final long intervalMinMillis,
	        @Named("wtm.strategy.betadist.intervalMaxMillis") final long intervalMaxMillis,
	        @Named("wtm.strategy.betadist.betaDistParamA") final double betaDistParamA,
	        @Named("wtm.strategy.betadist.betaDistParamB") final double betaDistParamB) {
		super(intervalMinMillis, intervalMaxMillis);
		this.betaDistParamA = betaDistParamA;
		this.betaDistParamB = betaDistParamB;
	}

	@Override
	public long calculateWaitingTime() {
		BetaDistribution betaDist = new BetaDistribution(betaDistParamA, betaDistParamB);
		double probability = betaDist.cumulative(Math.random());
		return calculateNormedValue(probability);
	}
}
