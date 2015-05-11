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
package com.mgmtp.perfload.core.client.web.okhttp;

import java.net.CookieHandler;
import java.net.InetAddress;

import javax.inject.Provider;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.google.inject.Inject;
import com.mgmtp.perfload.core.client.web.net.LocalAddressSocketFactory;
import com.squareup.okhttp.OkHttpClient;

/**
 * A JSR 330 provider for {@link OkHttpClient} instances.
 *
 * @author rnaegele
 */
public class OkHttpClientProvider implements Provider<OkHttpClient> {

	private SSLSocketFactory sslSocketFactory;
	private HostnameVerifier hostnameVerifier;
	private final CookieHandler cookieHandler;
	private final Provider<InetAddress> localAddressProvider;

	/**
	 * Sets an optinal {@link HostnameVerifier}.
	 *
	 * @param hostnameVerifier
	 *            the hostname verifier
	 */
	@Inject(optional = true)
	public void setHostnameVerifier(final HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	/**
	 * Sets an optinal {@link SSLSocketFactory}.
	 *
	 * @param sslSocketFactory
	 *            the ssl socket factory
	 */
	@Inject(optional = true)
	public void setSslSocketFactory(final SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	/**
	 * @param cookieHandler
	 *            the cookie handler
	 * @param localAddressProvider
	 *            the local address provider
	 */
	@Inject
	protected OkHttpClientProvider(final CookieHandler cookieHandler, final Provider<InetAddress> localAddressProvider) {
		this.cookieHandler = cookieHandler;
		this.localAddressProvider = localAddressProvider;
	}

	/**
	 * Creates a new OkHttpClient instance.
	 * 
	 * @return the OkHttpClient
	 */
	@Override
	public OkHttpClient get() {
		OkHttpClient client = new OkHttpClient();
		client.setFollowRedirects(true);
		client.setFollowSslRedirects(true);
		if (sslSocketFactory != null) {
			client.setSslSocketFactory(sslSocketFactory);
		}
		if (hostnameVerifier != null) {
			client.setHostnameVerifier(hostnameVerifier);
		}
		client.setCookieHandler(cookieHandler);
		client.setSocketFactory(new LocalAddressSocketFactory(SocketFactory.getDefault(), localAddressProvider));
		return client;
	}
}
