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
package com.mgmtp.perfload.core.testapp;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

/**
 * <p>
 * Class for computing FibonacciComputer numbers using a recursive algorithm and caching already computed numbers for better
 * performance.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 * 
 * @author rnaegele
 */
public class FibonacciComputer implements Serializable {
	private static final long serialVersionUID = 1L;

	private final ConcurrentMap<Integer, BigInteger> fibonacciCache = new MapMaker().makeMap();

	public FibonacciComputer() {
		fibonacciCache.put(0, BigInteger.ZERO);
		fibonacciCache.put(1, BigInteger.ONE);
	}

	/**
	 * Computes a FibonacciComputer number.
	 * 
	 * @param n
	 *            Specifies which FibonacciComputer number to compute
	 * @return The result
	 */
	public BigInteger compute(final int n) {
		if (n >= fibonacciCache.size()) {
			fibonacciCache.put(n, compute(n - 1).add(compute(n - 2)));
		}
		return fibonacciCache.get(n);
	}
}
