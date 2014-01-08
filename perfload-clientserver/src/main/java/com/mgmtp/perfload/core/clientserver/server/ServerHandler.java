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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;

/**
 * The {@link ChannelUpstreamHandler} implementation for {@link DefaultServer}s. Dispatches incoming
 * messages to registered {@link ServerMessageListener}s.
 * 
 * @author rnaegele
 */
final class ServerHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ChannelContainer channelContainer;

	// Must use a CopyOnWriteArraySet to guarantee thread safety
	private final Set<ServerMessageListener> listeners = new CopyOnWriteArraySet<>();

	/**
	 * @param channelContainer
	 *            The channel container that contains channels to all connected clients
	 */
	public ServerHandler(final ChannelContainer channelContainer) {
		checkArgument(channelContainer != null, "No ChannelContainer specified.");
		this.channelContainer = channelContainer;
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * Overriden to log ChannelStateEvents if they have a state other than
	 * {@link ChannelState#INTEREST_OPS}, i. e. OPEN, BOUND, CONNECTED.
	 * </p>
	 * 
	 * @param ctx
	 *            the context object for this handler
	 * @param e
	 *            the upstream event to process or intercept
	 */
	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			ChannelStateEvent stateEvent = (ChannelStateEvent) e;
			if (stateEvent.getState() != ChannelState.INTEREST_OPS) {
				log.info(e.toString());
				if (stateEvent.getState() == ChannelState.CONNECTED && stateEvent.getValue() == null) {
					// Remove channel from container when client disconnects
					channelContainer.removeChannel(e.getChannel());
				}
			}

		}
		super.handleUpstream(ctx, e);
	}

	/**
	 * Delegates to registered {@link ServerMessageListener}s.
	 * 
	 * @param ctx
	 *            the context object for this handler
	 * @param e
	 *            the upstream event to process or intercept
	 */
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
		for (ServerMessageListener listener : listeners) {
			listener.messageReceived(ctx, channelContainer, e);
		}
	}

	/**
	 * Logs the exception.
	 * 
	 * @param ctx
	 *            the context object for this handler
	 * @param e
	 *            the upstream event to process or intercept
	 */
	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
		log.error("Unexpected exception from downstream.", e.getCause());
	}

	/**
	 * Adds a {@link ServerMessageListener}.
	 * 
	 * @param listener
	 *            The listener object
	 */
	public void addServerMessageListener(final ServerMessageListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a {@link ServerMessageListener}.
	 * 
	 * @param listener
	 *            The listener object
	 */
	public void removeServerMessageListener(final ServerMessageListener listener) {
		listeners.remove(listener);
	}
}
