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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;

/**
 * Implementers may be registered with {@link DefaultServer}s in order to handle incoming messages.
 * 
 * @author rnaegele
 */
public interface ServerMessageListener {

	/**
	 * Reacts on an incoming message from a client. The {@code channelContainer} argument provides access to all connected
	 * channels in order to send messages to them in reaction to the incoming event.
	 * 
	 * @param ctx
	 *            the context object for this handler
	 * @param channelContainer
	 *            the channel container containing references to all connected channel
	 * @param e
	 *            the upstream event to process
	 */
	void messageReceived(final ChannelHandlerContext ctx, ChannelContainer channelContainer, final MessageEvent e);
}
