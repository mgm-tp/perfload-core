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
package com.mgmtp.perfload.core.console;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.converters.LongConverter;
import com.google.common.net.HostAndPort;
import com.mgmtp.perfload.core.console.util.HostAndPortConverter;

/**
 * Command-line arguments for the console for parsing with {@link JCommander}.
 * 
 * @author rnaegele
 */
public class LtConsoleArgs {

	@Parameter(names = "-testplan", required = true, description = "The name of the testplan xml file.")
	String testplan;

	@Parameter(names = "-abort", description = "Aborts a running test.")
	boolean abort;

	@Parameter(names = "-shutdownDaemons", description = "Causes daemons to be shut down after the test.")
	boolean shutdownDaemons = false;

	@Parameter(names = "-timeout", converter = LongConverter.class,
			description = "Timeout in minutes for aborting a test. Relative to the start time of the last load profile event.")
	long timeout = 15L;

	@Parameter(
			names = "-daemons",
			required = true,
			converter = HostAndPortConverter.class,
			splitter = CommaParameterSplitter.class,
			description = "The daemons (comma-separated) to use in the test (<host>:<port>). The port defaults to 20000 if omitted.")
	List<HostAndPort> daemons;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
