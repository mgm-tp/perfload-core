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
package com.mgmtp.perfload.core.clientserver.server;

import java.io.Serializable;

import com.google.common.base.Predicate;

public interface Server {

	/**
	 * Binds the servers to its port making it ready for accepting incoming connections.
	 */
	void bind();

	/**
	 * Gets the port this server runs on.
	 * 
	 * @return the port this server run on
	 */
	int getPort();

	/**
	 * Shuts down the server closing associated channels and resources.
	 */
	void shutdown();

	/**
	 * Asynchronously sends a message to all connected clients.
	 * 
	 * @param object
	 *            The message object
	 */
	void sendMessage(final Serializable object);

	/**
	 * Asynchronously sends a message to all clients matching the given filter predicate.
	 * 
	 * @param idFilter
	 *            The predicate for filtering the client channels based on their ids
	 * @param object
	 *            The message object
	 */
	void sendMessage(final Predicate<String> idFilter, final Serializable object);

	/**
	 * @see ServerHandler#addServerMessageListener(ServerMessageListener)
	 */
	void addServerMessageListener(final ServerMessageListener listener);

	/**
	 * @see ServerHandler#removeServerMessageListener(ServerMessageListener)
	 */
	void removeServerMessageListener(final ServerMessageListener listener);

}