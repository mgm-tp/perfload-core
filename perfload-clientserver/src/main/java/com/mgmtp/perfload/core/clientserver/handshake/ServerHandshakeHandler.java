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
package com.mgmtp.perfload.core.clientserver.handshake;

import static com.google.common.base.Preconditions.checkState;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;

/**
 * Handshake handler for the server side.
 * 
 * @author rnaegele
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 */
public final class ServerHandshakeHandler extends AbstractHandshakeHandler {

	private final ChannelContainer channelContainer;

	/**
	 * @param channelContainer
	 *            the container object for keeping connected channels
	 * @param timeoutInMillis
	 *            the timeout for the handshake
	 */
	public ServerHandshakeHandler(final ChannelContainer channelContainer, final long timeoutInMillis) {
		super(timeoutInMillis);
		this.channelContainer = channelContainer;
	}

	@Override
	protected void handleHandshakeMessage(final ChannelHandlerContext ctx, final MessageEvent e) {
		// Simply return the received handshake
		Handshake handshake = (Handshake) e.getMessage();
		writeDownstream(ctx, handshake);

		// After the handshake we can remove the handshake handler
		// from the pipeline because it is no longer needed.
		log.debug("Removing handshake handler from pipeline.");
		ctx.getPipeline().remove(this);
		channelContainer.addChannel(handshake.getClientId(), ctx.getChannel());
		fireHandshakeSucceeded(handshake, ctx);
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
		log.info("Connection established to: {}", e.getChannel().getRemoteAddress());

		// Fire up the handshake handler timeout checker.
		// Wait X seconds for the handshake then disconnect.
		new Thread() {

			@Override
			public void run() {
				try {
					latch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
				} catch (InterruptedException ex) {
					log.error(ex.getMessage(), ex);
				}

				// Already failed or successful, so return.
				if (handshakeCompletedOrFailed()) {
					return;
				}

				synchronized (handshakeMutex) {
					if (handshakeCompletedOrFailed()) {
						return;
					}

					log.warn("Handshake timeout expired. Triggering handshake failure...");
					ctx.sendUpstream(HandshakeEvent.handshakeFailed(ctx.getChannel()));
					handshakeFailed.set(true);
					ctx.getChannel().close();
				}
			}
		}.start();
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		synchronized (handshakeMutex) {
			if (handshakeFailed.get()) {
				// If the handshake failed meanwhile, discard any messages.
				return;
			}

			checkState(handshakeComplete.get(), "Cannot write before handshake completed.");
			super.writeRequested(ctx, e);
		}
	}

	private void writeDownstream(final ChannelHandlerContext ctx, final Object data) {
		ChannelFuture f = Channels.succeededFuture(ctx.getChannel());
		SocketAddress address = ctx.getChannel().getRemoteAddress();
		Channel c = ctx.getChannel();
		ctx.sendDownstream(new DownstreamMessageEvent(c, f, data, address));
	}
}
