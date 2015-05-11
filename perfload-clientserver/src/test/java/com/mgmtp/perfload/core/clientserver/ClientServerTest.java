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
package com.mgmtp.perfload.core.clientserver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.mgmtp.perfload.core.clientserver.client.Client;
import com.mgmtp.perfload.core.clientserver.client.ClientMessageListener;
import com.mgmtp.perfload.core.clientserver.client.DefaultClient;
import com.mgmtp.perfload.core.clientserver.server.DefaultServer;
import com.mgmtp.perfload.core.clientserver.server.Server;
import com.mgmtp.perfload.core.clientserver.server.ServerMessageListener;
import com.mgmtp.perfload.core.clientserver.util.ChannelContainer;

/**
 * @author rnaegele
 */
public class ClientServerTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final int PORT = 4242;
	private final Server server = new DefaultServer(PORT);

	@BeforeMethod
	public void startServer() {
		server.bind();
	}

	@AfterMethod
	public void shutdownServer() {
		server.shutdown();
	}

	@Test
	public void testCommunication() throws InterruptedException {
		assertEquals(server.getPort(), PORT);

		final CountDownLatch clientLatch = new CountDownLatch(4);
		final CountDownLatch serverLatch = new CountDownLatch(3);

		ClientMessageListener clientListener = new ClientMessageListener() {
			@Override
			public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) {
				log.debug(e.toString());
				clientLatch.countDown();
			}
		};

		ServerMessageListener serverListener = new ServerMessageListener() {
			@Override
			public void messageReceived(final ChannelHandlerContext ctx, final ChannelContainer channelContainer,
			        final MessageEvent e) {
				log.debug(e.toString());
				serverLatch.countDown();
			}
		};

		server.addServerMessageListener(serverListener);

		final Client consoleClient = new DefaultClient("console", "localhost", PORT);
		consoleClient.addClientMessageListener(clientListener);
		assertEquals("console", consoleClient.getClientId());

		final Client client1 = new DefaultClient("testproc1", "localhost", PORT);
		client1.addClientMessageListener(clientListener);
		assertEquals("testproc1", client1.getClientId());

		final Client client2 = new DefaultClient("testproc2", "localhost", PORT);
		client2.addClientMessageListener(clientListener);
		assertEquals("testproc2", client2.getClientId());

		consoleClient.connect();
		client1.connect();
		client2.connect();

		consoleClient.removeClientMessageListener(clientListener);
		assertFalse(clientLatch.await(1L, TimeUnit.SECONDS));
		consoleClient.addClientMessageListener(clientListener);

		server.removeServerMessageListener(serverListener);
		assertFalse(serverLatch.await(1L, TimeUnit.SECONDS));
		server.addServerMessageListener(serverListener);

		assertTrue(consoleClient.isConnected());
		assertTrue(client1.isConnected());
		assertTrue(client2.isConnected());

		consoleClient.sendMessage("foo");
		client1.sendMessage("foo");
		client2.sendMessage("foo");

		if (!serverLatch.await(1L, TimeUnit.SECONDS)) {
			fail();
		}

		server.sendMessage("foo");
		server.sendMessage(Predicates.equalTo("console"), "foo");

		if (!clientLatch.await(1L, TimeUnit.SECONDS)) {
			fail();
		}

		client1.disconnect();
		client2.disconnect();
		consoleClient.disconnect();

		assertFalse(consoleClient.isConnected());
		assertFalse(client1.isConnected());
		assertFalse(client2.isConnected());
	}
}
