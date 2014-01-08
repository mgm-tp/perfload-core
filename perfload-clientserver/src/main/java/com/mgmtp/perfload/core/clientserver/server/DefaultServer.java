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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.mgmtp.perfload.core.clientserver.handshake.ServerHandshakeHandler;
import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;
import com.mgmtp.perfload.core.clientserver.util.DaemonThreadFactory;

/**
 * Represents a server. Messages to the clients are sent asynchronously. A
 * {@link ServerMessageListener} must be used to handle incoming messages from the clients.
 * 
 * @author rnaegele
 */
public final class DefaultServer implements Server {

	private static final long CHANNEL_CLOSING_TIMEOUT_SECONDS = 5L;
	private static final long HANDSHAKE_TIMEOUT_MILLIS = 10000000L;
	private static final int DECODER_ESTIMATED_LENGTH = 20971520; // 20 MB max. size - should be largely sufficient
	private static final int ENCODER_ESTIMATED_LENGTH = 1048576; // 1 MB default size

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final int port;
	private final ServerBootstrap bootstrap;
	private final ChannelContainer channelContainer = new ChannelContainer();
	private final ServerHandler serverHandler = new ServerHandler(channelContainer);

	private Channel serverChannel;

	/**
	 * Creates a new server instance on the specified port.
	 * 
	 * @param port
	 *            The port
	 */
	public DefaultServer(final int port) {
		this.port = port;

		// Configure the server.
		bootstrap = new ServerBootstrap(
				new OioServerSocketChannelFactory(
						Executors.newCachedThreadPool(new DaemonThreadFactory()),
						Executors.newCachedThreadPool(new DaemonThreadFactory())));

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectEncoder(ENCODER_ESTIMATED_LENGTH), // 1 MB default size
						new ObjectDecoder(DECODER_ESTIMATED_LENGTH, ClassResolvers.weakCachingResolver(null)), // 20 MB max. size - should be largely sufficient
						new ServerHandshakeHandler(channelContainer, HANDSHAKE_TIMEOUT_MILLIS),
						serverHandler);
			}
		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
	}

	@Override
	public synchronized void bind() {
		checkArgument(serverChannel == null, "Server channel already assigned.");

		// Bind and start to accept incoming connections.
		serverChannel = bootstrap.bind(new InetSocketAddress(port));
		log.info("Successfully bound server to port {}", port);
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public synchronized void shutdown() {
		try {
			if (serverChannel != null && serverChannel.isOpen()) {
				serverChannel.close().awaitUninterruptibly(CHANNEL_CLOSING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			}
			for (Channel channel : channelContainer.getChannels()) {
				if (channel.isOpen()) {
					channel.close().awaitUninterruptibly(CHANNEL_CLOSING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
			}
		} finally {
			serverChannel = null;
			bootstrap.releaseExternalResources();
		}
	}

	@Override
	public void sendMessage(final Serializable object) {
		for (Channel channel : channelContainer.getChannels()) {
			if (channel.isConnected()) {
				channel.write(object);
			}
		}
	}

	@Override
	public void sendMessage(final Predicate<String> idFilter, final Serializable object) {
		for (Channel channel : channelContainer.getChannels(idFilter)) {
			if (channel.isConnected()) {
				channel.write(object);
			}
		}
	}

	@Override
	public void addServerMessageListener(final ServerMessageListener listener) {
		serverHandler.addServerMessageListener(listener);
	}

	@Override
	public void removeServerMessageListener(final ServerMessageListener listener) {
		serverHandler.removeServerMessageListener(listener);
	}

}