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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for handshake handlers.
 * 
 * @author rnaegele
 */
public abstract class AbstractHandshakeHandler extends SimpleChannelHandler {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final long timeoutInMillis;
	protected final AtomicBoolean handshakeComplete = new AtomicBoolean(false);
	protected final AtomicBoolean handshakeFailed = new AtomicBoolean(false);
	protected final CountDownLatch latch = new CountDownLatch(1);
	protected final Object handshakeMutex = new int[1];

	/**
	 * @param timeoutInMillis
	 *            the timeout for the handshake
	 */
	public AbstractHandshakeHandler(final long timeoutInMillis) {
		super();
		this.timeoutInMillis = timeoutInMillis;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		if (handshakeFailed.get()) {
			// Bail out fast if handshake already failed
			return;
		}

		if (handshakeComplete.get()) {
			// If handshake succeeded but message still came through this
			// handler, then immediately send it upwards.
			// Chances are it's the last time a message passes through
			// this handler...
			super.messageReceived(ctx, e);
			return;
		}

		synchronized (handshakeMutex) {
			// Re-check conditions after locking the mutex.
			// Things might have changed while waiting for the lock.
			if (handshakeFailed.get()) {
				return;
			}

			if (handshakeComplete.get()) {
				super.messageReceived(ctx, e);
				return;
			}

			handleHandshakeMessage(ctx, e);
		}
	}

	protected boolean handshakeCompletedOrFailed() {
		return handshakeFailed.get() || handshakeComplete.get();
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
		log.error(e.toString()); // we don't need to log the full stacktrace here
		if (e.getChannel().isConnected()) {
			// Closing the channel will trigger handshake failure.
			e.getChannel().close();
		} else {
			// Channel didn't open, so we must fire handshake failure directly.
			fireHandshakeFailed(ctx);
		}
	}

	@Override
	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
		if (!handshakeComplete.get()) {
			fireHandshakeFailed(ctx);
		}
	}

	/**
	 * Fires an event for handshake failure.
	 * 
	 * @param ctx
	 *            the context
	 */
	protected void fireHandshakeFailed(final ChannelHandlerContext ctx) {
		handshakeComplete.set(true);
		handshakeFailed.set(true);
		latch.countDown();
		ctx.getChannel().close();
		HandshakeEvent e = HandshakeEvent.handshakeFailed(ctx.getChannel());
		log.info(e.toString());
		ctx.sendUpstream(e);
	}

	/**
	 * Fires an event for handshake success.
	 * 
	 * @param handshake
	 *            the handshake instance
	 * @param ctx
	 *            the context
	 */
	protected void fireHandshakeSucceeded(final Handshake handshake, final ChannelHandlerContext ctx) {
		handshakeComplete.set(true);
		handshakeFailed.set(false);
		latch.countDown();
		HandshakeEvent e = HandshakeEvent.handshakeSucceeded(handshake, ctx.getChannel());
		log.info(e.toString());
		ctx.sendUpstream(e);
	}

	protected abstract void handleHandshakeMessage(final ChannelHandlerContext ctx, final MessageEvent e);
}
