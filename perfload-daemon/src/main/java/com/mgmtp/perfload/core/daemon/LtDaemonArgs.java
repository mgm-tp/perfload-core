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
package com.mgmtp.perfload.core.daemon;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Command-line arguments for the daemon for parsing with {@link JCommander}.
 * 
 * @author rnaegele
 */
public class LtDaemonArgs {

	@Parameter(names = "-port", required = true, description = "The port for the daemon server.")
	int port = 20000;

	@Parameter(names = "-shutdown", description = "Shuts down a running daemon.")
	boolean shutdown;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
