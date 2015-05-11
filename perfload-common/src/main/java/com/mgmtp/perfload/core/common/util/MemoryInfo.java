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
package com.mgmtp.perfload.core.common.util;

/**
 * Simple helper class to log JVM runtime memory information.
 * 
 * @author jdost
 */
public final class MemoryInfo {

	/**
	 * Enum values can be used for customizing the output.
	 */
	public static enum Unit {

		/**
		 * Format output in Bytes.
		 */
		BYTES(1, " B"),

		/**
		 * Format output in Kilobytes.
		 */
		KILOBYTES(1000, " KB"),

		/**
		 * Format output in Kibibytes.<br>
		 * <a href ="http://en.wikipedia.org/wiki/Kibibyte">
		 */
		KIBIBYTES(1024, " KiB"),

		/**
		 * Format output in Megabytes.
		 */
		MEGABYTES(1000 * 1000, " MB"),

		/**
		 * Format output in Mebibytes.<br>
		 * <a href ="http://en.wikipedia.org/wiki/Mebibyte">
		 */
		MEBIBYTES(1024 * 1024, " MiB");

		private final int denominator;
		private final String unitString;

		private Unit(final int denominator, final String unitString) {
			this.denominator = denominator;
			this.unitString = unitString;
		}

		/**
		 * @return the denominator
		 */
		public int getDenominator() {
			return denominator;
		}

		/**
		 * @return the unitString
		 */
		public String getUnitString() {
			return unitString;
		}
	}

	private static enum Type {

		/**
		 * References the free memory.
		 */
		FREE,

		/**
		 * References the max memory.
		 */
		TOTAL,

		/**
		 * References the total memory.
		 */
		MAX;
	}

	private MemoryInfo() {
		// Class must not be instantiated
	}

	private static long[] getMemoryInfo() {
		final long[] memory = new long[Type.values().length];
		memory[Type.FREE.ordinal()] = Runtime.getRuntime().freeMemory();
		memory[Type.TOTAL.ordinal()] = Runtime.getRuntime().totalMemory();
		memory[Type.MAX.ordinal()] = Runtime.getRuntime().maxMemory();
		return memory;
	}

	/**
	 * Get information about the current memory status of the JVM.
	 * 
	 * @param unit
	 *            used for formatting. Valid values are:
	 *            <ul>
	 *            <li>{@link Unit#BYTES}</li>
	 *            <li>{@link Unit#KILOBYTES}</li>
	 *            <li>{@link Unit#KIBIBYTES}</li>
	 *            <li>{@link Unit#MEGABYTES}</li>
	 *            <li>{@link Unit#MEBIBYTES}</li>
	 *            </ul>
	 *            If no value is given, {@link Unit#BYTES} will be used. No decimal place will be calculated, plain integer values
	 *            are returned.
	 * @return a string with the current memory information
	 */
	public static String getMemoryInfo(final Unit unit) {
		final long[] memory = getMemoryInfo();

		for (final Type type : Type.values()) {
			memory[type.ordinal()] /= unit.getDenominator();
		}

		final StringBuilder sb = new StringBuilder(100);
		sb.append("Memory (free/total/max): ");
		sb.append(memory[Type.FREE.ordinal()]);
		sb.append(unit.getUnitString());
		sb.append("/");
		sb.append(memory[Type.TOTAL.ordinal()]);
		sb.append(unit.getUnitString());
		sb.append("/");
		sb.append(memory[Type.MAX.ordinal()]);
		sb.append(unit.getUnitString());
		return sb.toString();
	}
}