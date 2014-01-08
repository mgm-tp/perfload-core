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
package com.mgmtp.perfload.core.console.util;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import com.google.common.net.HostAndPort;

/**
 * @author rnaegele
 */
public class HostAndPortConverter extends BaseConverter<HostAndPort> {
	public static final int DEFAULT_PORT = 20000;

	public HostAndPortConverter(final String optionName) {
		super(optionName);
	}

	@Override
	public HostAndPort convert(final String value) {
		try {
			return HostAndPort.fromString(value).withDefaultPort(DEFAULT_PORT);
		} catch (IllegalStateException ex) {
			throw new ParameterException(getErrorString(value, "host and port"));
		}
	}
}
