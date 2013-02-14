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
package com.mgmtp.perfload.core.common.clientserver;

import static com.mgmtp.perfload.core.common.util.LtUtils.toDefaultString;

import java.io.Serializable;

/**
 * Class for transferring objects between clients and server.
 * 
 * @author rnaegele
 */
public class Payload implements Serializable {
	private static final long serialVersionUID = 42;

	private final PayloadType payloadType;
	private final Serializable content;

	/**
	 * Creates a new instance with the specified type and content.
	 * 
	 * @param payloadType
	 *            The transfer type
	 * @param content
	 *            The content
	 */
	public Payload(final PayloadType payloadType, final Serializable content) {
		this.payloadType = payloadType;
		this.content = content;
	}

	/**
	 * Creates an empty instance with the specified type.
	 * 
	 * @param transferType
	 *            The transfer type
	 */
	public Payload(final PayloadType transferType) {
		this(transferType, null);
	}

	public PayloadType getPayloadType() {
		return payloadType;
	}

	public Serializable getContent() {
		return content;
	}

	@Override
	public String toString() {
		return toDefaultString(this);
	}
}
