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
package com.mgmtp.perfload.core.client.web.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.inject.Provider;
import javax.net.SocketFactory;

/**
 * @author rnaegele
 */
public class LocalAddressSocketFactory extends SocketFactory {
	private static final String ERROR_MSG = "On unconnected sockets are supported";

	private final SocketFactory delegate;
	private final Provider<InetAddress> localAddressProvider;

	public LocalAddressSocketFactory(final SocketFactory delegate, final Provider<InetAddress> localAddressProvider) {
		this.delegate = delegate;
		this.localAddressProvider = localAddressProvider;
	}

	@Override
	public Socket createSocket() throws IOException {
		Socket socket = delegate.createSocket();
		socket.bind(new InetSocketAddress(localAddressProvider.get(), 0));
		return socket;
	}

	@Override
	public Socket createSocket(final String remoteAddress, final int remotePort) throws IOException, UnknownHostException {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public Socket createSocket(final InetAddress remoteAddress, final int remotePort) throws IOException {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public Socket createSocket(final String remoteAddress, final int remotePort, final InetAddress localAddress, final int localPort)
			throws IOException, UnknownHostException {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public Socket createSocket(final InetAddress remoteAddress, final int remotePort, final InetAddress localAddress, final int localPort)
			throws IOException {
		throw new UnsupportedOperationException(ERROR_MSG);
	}
}
