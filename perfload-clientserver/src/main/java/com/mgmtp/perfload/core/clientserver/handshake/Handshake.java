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
package com.mgmtp.perfload.core.clientserver.handshake;

import java.io.Serializable;

/**
 * Simple POJO used for handshakes. The handshake is kept simple without any challenge-response
 * scenario.
 * 
 * @author rnaegele
 */
public final class Handshake implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String clientId;

	/**
	 * @param clientId
	 *            The id of the client that wants to connect to the server.
	 */
	public Handshake(final String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clientId == null ? 0 : clientId.hashCode());
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
		Handshake other = (Handshake) obj;
		if (clientId == null) {
			if (other.clientId != null) {
				return false;
			}
		} else if (!clientId.equals(other.clientId)) {
			return false;
		}
		return true;
	}
}
