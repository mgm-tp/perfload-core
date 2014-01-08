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
package com.mgmtp.perfload.core.clientserver.handshake;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

/**
 * Handshake handler for the client side.
 * 
 * @author rnaegele
 * @author <a href="mailto:bruno@factor45.org">Bruno de Carvalho</a>
 */
public final class ClientHandshakeHandler extends AbstractHandshakeHandler {

	private final Queue<MessageEvent> messages = new ArrayDeque<>();
	private final Handshake handshake;

	public ClientHandshakeHandler(final Handshake handshake, final long timeoutInMillis) {
		super(timeoutInMillis);
		this.handshake = handshake;
	}

	@Override
	protected void handleHandshakeMessage(final ChannelHandlerContext ctx, final MessageEvent e) {
		// Check that the same handshake is returned from the server.
		// A more advanced challenge response algorithm is not necessary.
		Handshake receivedHandshake = (Handshake) e.getMessage();
		if (!handshake.equals(receivedHandshake)) {
			fireHandshakeFailed(ctx);
			return;
		}

		// Everything went okay!
		log.debug("Server returned correct id.");

		// Flush messages *directly* downwards.
		// Calling ctx.getChannel().write() here would cause the messages
		// to be inserted at the top of the pipeline, thus causing them
		// to pass through this class's writeRequest() and be re-queued.
		log.debug("Flushing {} messages that have been enqueued.", messages.size());
		for (MessageEvent message : messages) {
			ctx.sendDownstream(message);
		}

		// After the handshake we can remove the handshake handler
		// from the pipeline because it is no longer needed.
		log.debug("Removing handshake handler from pipeline.");
		ctx.getPipeline().remove(this);

		// Finally fire success message upwards.
		fireHandshakeSucceeded(handshake, ctx);
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
		log.info("Connection established to: {}", e.getChannel().getRemoteAddress());

		// Write the handshake add a timeout for the handshake.
		ChannelFuture channelFuture = Channels.future(ctx.getChannel());
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				// Once this message is sent, start the timeout checker.
				new Thread() {
					@Override
					public void run() {
						// Wait until either handshake completes (releases the latch) or this latch times out.
						try {
							latch.await(timeoutInMillis, TimeUnit.MILLISECONDS);
						} catch (InterruptedException ex) {
							log.error(ex.getMessage(), ex);
						}

						// Already failed or successful, so return.
						if (handshakeCompletedOrFailed()) {
							return;
						}

						// Timout expired, so we may need to trigger handshake failure.
						synchronized (handshakeMutex) {
							if (handshakeCompletedOrFailed()) {
								return;
							}

							log.warn("Handshake timeout expired. Triggering handshake failure...");
							fireHandshakeFailed(ctx);
						}
					}
				}.start();
			}
		});

		// Passing null as remoteAddress, since constructor in
		// DownstreamMessageEvent will use remote address from the channel if
		// remoteAddress is null.
		// Also, we need to send the data directly downstream rather than
		// call c.write() otherwise the message would pass through this
		// class's writeRequested() method defined below.
		ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), channelFuture, handshake, null));
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		// Before doing anything, ensure that noone else is working by
		// acquiring a lock on the handshakeMutex.
		synchronized (handshakeMutex) {
			if (handshakeFailed.get()) {
				// If the handshake failed meanwhile, discard any messages.
				return;
			}

			// If the handshake hasn't failed but completed meanwhile and messages still passed through this handler,
			// then forward them downwards.
			if (handshakeComplete.get()) {
				log.debug("Handshake already completed. Message does not have to be enqueued.");
				super.writeRequested(ctx, e);
			} else {
				// Otherwise, enqueue messages in order until the handshake completes.
				checkState(messages.offer(e), "Could not enqueue pending messages during handshake.");
			}
		}
	}
}
