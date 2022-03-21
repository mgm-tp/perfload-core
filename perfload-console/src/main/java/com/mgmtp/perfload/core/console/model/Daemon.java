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
package com.mgmtp.perfload.core.console.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.net.HostAndPort;

/**
 * @author rnaegele
 */
public class Daemon implements Comparable<Daemon> {
	private final int id;
	private final String host;
	private final int port;

	public Daemon(final int id, final String host, final int port) {
		checkState(id > 0, "'id' must be greater than 0");
		checkState(port > 0, "'port' must be greater than 0");
		this.id = id;
		this.host = checkNotNull(host, "'host' must not be null");
		this.port = port;
	}

	public static Daemon fromHostAndPort(final int id, final HostAndPort hostAndPort) {
		String host = hostAndPort.getHost();
		int port = hostAndPort.getPort();
		return new Daemon(id, host, port);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	@Override
	public int compareTo(final Daemon o) {
		int result = id - o.id;
		if (result == 0) {
			result = host.compareTo(o.host);
		}
		if (result == 0) {
			result = port - o.port;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + host.hashCode();
		result = prime * result + id;
		result = prime * result + port;
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
		Daemon other = (Daemon) obj;
		if (!host.equals(other.host)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}
}
