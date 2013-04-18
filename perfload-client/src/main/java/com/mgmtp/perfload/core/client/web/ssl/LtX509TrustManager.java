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
package com.mgmtp.perfload.core.client.web.ssl;

import static com.google.common.base.Preconditions.checkArgument;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link X509TrustManager} implementation for SSL and client authentication. Acts as a decorator to
 * the * {@link X509TrustManager} passed in by the constructor in order to add logging.
 * 
 * @author rnaegele
 */
public final class LtX509TrustManager implements X509TrustManager {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final X509TrustManager defaultTrustManager;

	public LtX509TrustManager(final X509TrustManager defaultTrustManager) {
		checkArgument(defaultTrustManager != null, "Trust manager may not be null");
		this.defaultTrustManager = defaultTrustManager;
	}

	@Override
	public void checkClientTrusted(final X509Certificate[] certificates, final String authType) throws CertificateException {
		if (log.isDebugEnabled() && certificates != null) {
			for (int i = 0; i < certificates.length; ++i) {
				log.debug("Client certificate {}:", i + 1);
				logCertificate(certificates[i]);
			}
		}
		defaultTrustManager.checkClientTrusted(certificates, authType);
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] certificates, final String authType) throws CertificateException {
		if (log.isDebugEnabled() && certificates != null) {
			for (int i = 0; i < certificates.length; ++i) {
				log.debug("Server certificate {}:", i + 1);
				logCertificate(certificates[i]);
			}
		}
		defaultTrustManager.checkServerTrusted(certificates, authType);
	}

	private void logCertificate(final X509Certificate cert) {
		log.info("  Subject DN: {}", cert.getSubjectDN());
		log.info("  Signature algorithm name: {}", cert.getSigAlgName());
		log.info("  Valid from: {}", cert.getNotBefore());
		log.info("  Valid until: {}", cert.getNotAfter());
		log.info("  Issuer DN: {}", cert.getIssuerDN());
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.defaultTrustManager.getAcceptedIssuers();
	}
}
