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
package com.mgmtp.perfload.core.client.web.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author rnaegele
 */
public class SslTrustAllModule extends AbstractModule {

	@Override
	protected void configure() {
		// This avoids "javax.net.ssl.SSLProtocolException: handshake alert: unrecognized_name"
		// See http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	@Provides
	@Singleton
	protected SSLContext provideSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, new TrustManager[] { new TrustAllManager() }, null);
		return ctx;
	}

	@Provides
	@Singleton
	protected SSLSocketFactory provideSSLSocketFactory(final SSLContext sslContext) {
		return sslContext.getSocketFactory();
	}

	@Provides
	@Singleton
	protected HostnameVerifier provideHostnameVerifier() {
		return (hostname, session) -> true;
	}

	static final class TrustAllManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(final X509Certificate[] certificates, final String authType) throws CertificateException {
			// no-op
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] certificates, final String authType) throws CertificateException {
			// no-op
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
