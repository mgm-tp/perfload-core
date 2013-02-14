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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

/**
 * {@link ChannelEvent} implementation for handshakes.
 * 
 * @author rnaegele
 */
public final class HandshakeEvent implements ChannelEvent {

	private final boolean successful;
	private final Handshake handshake;
	private final Channel channel;

	private HandshakeEvent(final Handshake handshake, final Channel channel) {
		this.handshake = handshake;
		this.successful = handshake != null;
		this.channel = channel;
	}

	/**
	 * Factory method for a successful {@link HandshakeEvent}.
	 * 
	 * @param handshake
	 *            the handshake object
	 * @param channel
	 *            the associated channel
	 * @return the event
	 */
	public static HandshakeEvent handshakeSucceeded(final Handshake handshake, final Channel channel) {
		return new HandshakeEvent(handshake, channel);
	}

	/**
	 * Factory method for a failed {@link HandshakeEvent}.
	 * 
	 * @param channel
	 *            the associated channel
	 * @return the event
	 */
	public static HandshakeEvent handshakeFailed(final Channel channel) {
		return new HandshakeEvent(null, channel);
	}

	@Override
	public Channel getChannel() {
		return this.channel;
	}

	@Override
	public ChannelFuture getFuture() {
		return Channels.succeededFuture(this.channel);
	}

	public boolean isSuccessful() {
		return successful;
	}

	public Handshake getHandshake() {
		return handshake;
	}

	@Override
	public String toString() {
		String channelString = getChannel().toString();
		StringBuilder sb = new StringBuilder(channelString.length() + 64);
		sb.append(channelString);
		sb.append(" HANDSHAKE");
		sb.append(successful ? " SUCCESSFUL" : "FAILED");
		return sb.toString();
	}
}
