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
package com.mgmtp.perfload.test.utils;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelFuture;

import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.clientserver.client.ClientMessageListener;

/**
 * Mock client for running the a driver within a unit test.
 * 
 * @author rnaegele
 * @since 4.7.0
 */
public class MockClient implements Client {
	/**
	 * @return "client"
	 */
	@Override
	public String getClientId() {
		return "client";
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void connect() {
		//
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isConnected() {
		return true;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void disconnect() {
		//
	}

	/**
	 * Does nothing and return null.
	 * 
	 * @return null
	 */
	@Override
	public ChannelFuture sendMessage(final Serializable object) {
		return null;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void addClientMessageListener(final ClientMessageListener listener) {
		//
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void removeClientMessageListener(final ClientMessageListener listener) {
		//
	}
}