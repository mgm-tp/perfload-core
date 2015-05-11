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
package com.mgmtp.perfload.core.clientserver.client;

import java.io.Serializable;

import org.jboss.netty.channel.ChannelFuture;

import com.mgmtp.perfload.core.clientserver.server.Server;

/**
 * Client for a {@link Server}. Messages to the server are sent asynchronously. A {@link ClientMessageListener} must be used to
 * handle incoming messages from the server.
 * 
 * @author rnaegele
 */
public interface Client {

	/**
	 * @return the id of the client
	 */
	String getClientId();

	/**
	 * Connects to the server.
	 */
	void connect();

	/**
	 * @return {@code true} if the client is connected to a server.
	 */
	boolean isConnected();

	/**
	 * Disconnects from the server closing the channel to the server and releasing associated resources. The method checks whether
	 * the channel is still connected before potentially closing it.
	 */
	void disconnect();

	/**
	 * Asynchronously sends a message to the server.
	 * 
	 * @param object
	 *            The message object
	 */
	ChannelFuture sendMessage(final Serializable object);

	/**
	 * @see ClientHandler#addClientMessageListener(ClientMessageListener)
	 */
	void addClientMessageListener(final ClientMessageListener listener);

	/**
	 * @see ClientHandler#removeClientMessageListener(ClientMessageListener)
	 */
	void removeClientMessageListener(final ClientMessageListener listener);
}
