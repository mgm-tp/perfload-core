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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.web.ssl.LtX509TrustManager;

/**
 * @author rnaegele
 */
public class LtX509TrustManagerTest {

	@SuppressWarnings("unused")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullTrustManager() {
		new LtX509TrustManager(null);
	}

	@Test
	public void testTrustManager() throws CertificateException {
		X509TrustManager tm = mock(X509TrustManager.class);
		LtX509TrustManager decorator = new LtX509TrustManager(tm);
		X509Certificate[] certs = new X509Certificate[] { mock(X509Certificate.class) };

		decorator.checkClientTrusted(certs, null);
		decorator.checkServerTrusted(certs, null);
		decorator.getAcceptedIssuers();

		verify(tm).checkClientTrusted(certs, null);
		verify(tm).checkServerTrusted(certs, null);
		verify(tm).getAcceptedIssuers();
	}
}
