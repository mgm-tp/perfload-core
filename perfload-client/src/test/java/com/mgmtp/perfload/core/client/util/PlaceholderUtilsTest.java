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

import static com.google.common.collect.Maps.newHashMap;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.testng.Assert.assertNull;

import java.text.ParsePosition;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author rnaegele
 */
public class PlaceholderUtilsTest {

	private final Map<String, String> replacements = newHashMap();

	@BeforeTest
	public void setUpReplacements() {
		replacements.put("foo", "foovalue");
		replacements.put("bar", "barvalue");
		replacements.put("baz", "bazvalue");
		replacements.put("empty", "");
		replacements.put("", "");
		replacements.put("null", null);
	}

	@Test
	public void testSinglePlaceholderString() {
		String input = "${foo}";
		String result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("foovalue");
	}

	@Test
	public void testMultiplePlaceholderStrings() {
		String input = "${foo}${bar}${baz}";
		String result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("foovaluebarvaluebazvalue");

		input = "_${foo}_${bar}_${baz}_";
		result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("_foovalue_barvalue_bazvalue_");

		input = "_${null}_${foo}_${bar}_${baz}_${empty}_${}_";
		result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("_${null}_foovalue_barvalue_bazvalue___");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testInvalidPlaceholder() {
		String input = "${foo";
		PlaceholderUtils.resolvePlaceholders(input, replacements);
	}

	@Test
	public void testNonExistingPlaceholder() {
		String input = "${blah}";
		String result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("${blah}");
	}

	@Test
	public void testNonExistingAmongstMultiplePlaceholders() {
		String input = "_${foo}_${blah}_${baz}_";
		String result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertThat(result).isEqualTo("_foovalue_${blah}_bazvalue_");
	}

	@Test
	public void testNullInput() {
		String input = null;
		String result = PlaceholderUtils.resolvePlaceholders(input, replacements);
		assertNull(result);
		result = PlaceholderUtils.resolveNextPlaceholder(input, new ParsePosition(0), replacements);
		assertNull(result);
	}

	@Test
	public void testResolveWithPositionGreaterThanInputLength() {
		String result = PlaceholderUtils.resolveNextPlaceholder("foo", new ParsePosition(5), replacements);
		assertNull(result);
	}
}
