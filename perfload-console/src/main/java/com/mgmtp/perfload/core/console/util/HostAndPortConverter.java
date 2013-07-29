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
