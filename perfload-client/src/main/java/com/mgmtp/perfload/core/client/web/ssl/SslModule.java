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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.inject.Singleton;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mgmtp.perfload.core.client.web.constants.WebConstants;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * @author rnaegele
 */
public class SslModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(SslModule.class);

	@Override
	protected void configure() {
		//
	}

	@Provides
	@Singleton
	protected SSLSocketFactory provideSSLSockerFactory(final PropertiesMap properties) throws NoSuchAlgorithmException, KeyManagementException,
	UnrecoverableKeyException, KeyStoreException, CertificateException, IOException {
		SSLContext ctx = SSLContext.getInstance("TLS");
		KeyManager[] keyManagers = createKeyManagers(properties);
		TrustManager[] trustManagers = createTrustManagers(properties);
		ctx.init(keyManagers, trustManagers, null);
		return ctx.getSocketFactory();
	}

	protected TrustManager[] createTrustManagers(final PropertiesMap properties) throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException {
		String trustStoreResource = properties.get(WebConstants.TRUST_STORE);
		String trustStorePassword = properties.get(WebConstants.TRUST_STORE_PASSWORD);
		String trustStoreType = properties.get(WebConstants.TRUST_STORE_TYPE);

		if (trustStoreResource == null) {
			LOGGER.warn("'{}' is null", WebConstants.TRUST_STORE);
			return null;
		}

		KeyStore store = createStore(trustStoreResource, trustStorePassword, trustStoreType);
		TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmfactory.init(store);

		return tmfactory.getTrustManagers();
	}

	protected KeyManager[] createKeyManagers(final PropertiesMap properties) throws KeyStoreException,
			NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, IOException {

		String keyStoreResource = properties.get(WebConstants.KEY_STORE);
		String keyStorePassword = properties.get(WebConstants.KEY_STORE_PASSWORD);
		String keyStoreType = properties.get(WebConstants.KEY_STORE_TYPE);

		if (keyStoreResource == null) {
			LOGGER.warn("'{}' is null", WebConstants.KEY_STORE);
			return null;
		}

		KeyStore store = createStore(keyStoreResource, keyStorePassword, keyStoreType);
		KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmfactory.init(store, keyStorePassword != null ? keyStorePassword.toCharArray() : null);

		return kmfactory.getKeyManagers();
	}

	protected KeyStore createStore(final String resource, final String password, final String type) throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {

		URL trustStoreUrl = Resources.getResource(resource);
		char[] trustStorePassword = password != null ? password.toCharArray() : null;
		String trustStoreType = type != null ? type : "jks";

		KeyStore keystore = KeyStore.getInstance(trustStoreType);
		try (InputStream is = trustStoreUrl.openStream()) {
			keystore.load(is, trustStorePassword);

			if (LOGGER.isDebugEnabled()) {
				for (Enumeration<String> aliases = keystore.aliases(); aliases.hasMoreElements();) {
					String alias = aliases.nextElement();
					Certificate[] certs = keystore.getCertificateChain(alias);
					if (certs != null) {
						LOGGER.debug("Certificate chain '{}':", alias);
						for (int i = 0; i < certs.length; ++i) {
							LOGGER.debug(certs[i].toString());
						}
					}
				}
			}

			return keystore;
		}
	}
}
