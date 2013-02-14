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
package com.mgmtp.perfload.core.clientserver.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.mgmtp.perfload.core.clientserver.handshake.ClientHandshakeHandler;
import com.mgmtp.perfload.core.clientserver.handshake.Handshake;
import com.mgmtp.perfload.core.clientserver.server.DefaultServer;
import com.mgmtp.perfload.core.clientserver.util.DaemonThreadFactory;

/**
 * Client for the server. Messages to the server are sent asynchronously. A
 * {@link ClientMessageListener} must be used to handle incoming messages from the server.
 * 
 * @author rnaegele
 */
public final class DefaultClient implements Client {

	private static final long CHANNEL_CLOSING_TIMEOUT_SECONDS = 5L;
	private static final long HANDSHAKE_TIMEOUT_MILLIS = 10000000L;
	private static final int DECODER_ESTIMATED_LENGTH = 20971520; // 20 MB max. size - should be largely sufficient
	private static final int ENCODER_ESTIMATED_LENGTH = 1048576; // 1 MB default size

	private final String clientId;
	private final String host;
	private final int port;
	private final ClientBootstrap bootstrap;
	private final ClientHandler clientHandler = new ClientHandler();

	private volatile Channel channel;

	/**
	 * Creates a new instance that talks to a {@link DefaultServer} on the specified host and port.
	 * 
	 * @param clientId
	 *            A unique id. No two clients with the same id can access the same
	 *            {@link DefaultServer}
	 * @param host
	 *            The host the {@link DefaultServer} runs on
	 * @param port
	 *            The port the {@link DefaultServer} runs on
	 */
	public DefaultClient(final String clientId, final String host, final int port) {
		this.clientId = clientId;
		this.host = host;
		this.port = port;

		// Configure the client.
		bootstrap = new ClientBootstrap(
				new OioClientSocketChannelFactory(Executors.newCachedThreadPool(new DaemonThreadFactory())));

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectEncoder(ENCODER_ESTIMATED_LENGTH),
						new ObjectDecoder(DECODER_ESTIMATED_LENGTH, ClassResolvers.weakCachingResolver(null)),
						new ClientHandshakeHandler(new Handshake(clientId), HANDSHAKE_TIMEOUT_MILLIS),
						clientHandler);
			}
		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("soTimeout", 10000L);
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	/**
	 * Connects to the server.
	 */
	@Override
	public synchronized void connect() {
		checkArgument(channel == null, "Client seems to already be connected.");
		channel = bootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly().getChannel();
	}

	@Override
	public boolean isConnected() {
		Channel ch = channel;
		return ch != null && ch.isConnected();
	}

	/**
	 * Disconnects from the server closing the channel to the server and releasing associated
	 * resources. The method checks whether the channel is still open before potentially closing it.
	 */
	@Override
	public void disconnect() {
		try {
			Thread.sleep(500L);
		} catch (InterruptedException ex) {
			// ignore
		}
		synchronized (this) {
			try {
				if (channel.isOpen()) {
					channel.close().awaitUninterruptibly(CHANNEL_CLOSING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
			} finally {
				channel = null;
				bootstrap.releaseExternalResources();
			}
		}
	}

	/**
	 * Asynchronously sends a message to the server.
	 * 
	 * @param object
	 *            The message object
	 */
	@Override
	public ChannelFuture sendMessage(final Serializable object) {
		return channel.write(object);
	}

	/**
	 * @see ClientHandler#addClientMessageListener(ClientMessageListener)
	 */
	@Override
	public void addClientMessageListener(final ClientMessageListener listener) {
		clientHandler.addClientMessageListener(listener);
	}

	/**
	 * @see ClientHandler#removeClientMessageListener(ClientMessageListener)
	 */
	@Override
	public void removeClientMessageListener(final ClientMessageListener listener) {
		clientHandler.removeClientMessageListener(listener);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
