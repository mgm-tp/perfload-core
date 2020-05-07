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

import javax.inject.Named;
import javax.inject.Provider;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.google.inject.Inject;
import com.mgmtp.perfload.core.client.web.net.LocalAddressSocketFactory;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.LoggerFactory;

/**
 * A JSR 330 provider for {@link OkHttpClient} instances.
 *
 * @author rnaegele
 */
public class OkHttpClientProvider implements Provider<OkHttpClient> {

	private SSLSocketFactory sslSocketFactory;
	private HostnameVerifier hostnameVerifier;
	private Dispatcher dispatcher;

	private final CookieHandler cookieHandler;
	private final Provider<InetAddress> localAddressProvider;
	// redirect behaviour has been made configurable
	private boolean followRedirects=true; // old setting still is default behaviour
	private static int redirectsLogged=0; // log the configured choice exactly once

	/**
	 * Sets an optional boolean whether to follow redirects (default) or not
	 *
	 * @param followRedirects
	 * If present value is taken from testplan.xml where it may be configured in the following way:
	 * <properties>
	 *      ...
	 *		<property name="followRedirects">false</property>
	 * </properties>
	 */
	@Inject(optional = true)
	public void setFollowRedirects(@Named("followRedirects") final String followRedirects) {
		if( redirectsLogged++<5 ) {
			LoggerFactory.getLogger(getClass()).warn("test runs with followRedirects=" + followRedirects);
		}
		this.followRedirects = Boolean.valueOf(followRedirects);
	}

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
	 * Set an optional {@link Dispatcher} for better control of asynchronous requests.
	 *
	 * @param dispatcher
	 *            the dispatcher
	 */
	@Inject(optional = true)
	public void setDispatcher(final Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
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
		client.setFollowRedirects(followRedirects);
		client.setFollowSslRedirects(followRedirects);
		client.setCookieHandler(cookieHandler);
		client.setSocketFactory(new LocalAddressSocketFactory(SocketFactory.getDefault(), localAddressProvider));
		if (sslSocketFactory != null) {
			client.setSslSocketFactory(sslSocketFactory);
		}
		if (hostnameVerifier != null) {
			client.setHostnameVerifier(hostnameVerifier);
		}
		if (dispatcher != null) {
			client.setDispatcher(dispatcher);
		}
		return client;
	}
}
