/*
 * Copyright (c) 2002-2014 mgm technology partners GmbH
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
package com.mgmtp.perfload.core.common.config;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.copyOf;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a jar file needed by a test process.
 * 
 * @author rnaegele
 */
public class TestJar implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;
	private final byte[] content;

	/**
	 * @param name
	 *            the file name
	 * @param content
	 *            the content of the file
	 */
	public TestJar(final String name, final byte[] content) {
		checkArgument(name != null, "'name' must not be null.");
		checkArgument(content != null && content.length > 0, "'content' must not be null or empty.");

		this.name = name;
		this.content = copyOf(content, content.length);
	}

	public String getName() {
		return name;
	}

	public byte[] getContent() {
		return copyOf(content, content.length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestJar other = (TestJar) obj;
		if (!Arrays.equals(content, other.content)) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
