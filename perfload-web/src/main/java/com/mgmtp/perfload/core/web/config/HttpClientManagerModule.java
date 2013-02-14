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
package com.mgmtp.perfload.core.web.config;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.LayeredSchemeSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mgmtp.perfload.core.client.config.annotations.PerfLoadVersion;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.mgmtp.perfload.core.web.http.DefaultHttpClientManager;
import com.mgmtp.perfload.core.web.http.HttpClientManager;
import com.mgmtp.perfload.core.web.http.RedirectAfterPostStrategy;
import com.mgmtp.perfload.core.web.ssl.LtSSLSocketFactory;
import com.mgmtp.perfload.core.web.ssl.TrustAllManager;

/**
 * Separate Guice module for {@link HttpClientManager} configuration.
 * 
 * @author rnaegele
 */
public final class HttpClientManagerModule extends AbstractModule {

	private static final String HTTPS = "https";

	private static final String SSL_TRUST_ALL = "ssl.trust.all";

	private static final String TRUST_STORE = "javax.net.ssl.trustStore";
	private static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
	private static final String TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";
	private static final String KEY_STORE = "javax.net.ssl.keyStore";
	private static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
	private static final String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";

	/**
	 * Creates a binding for {@link HttpClientManager} to {@link DefaultHttpClientManager}.
	 */
	@Override
	protected void configure() {
		try {
			Constructor<SingleClientConnManager> connMgrConstructor = SingleClientConnManager.class.getConstructor(SchemeRegistry.class);
			bind(ClientConnectionManager.class).toConstructor(connMgrConstructor);
		} catch (Exception ex) {
			addError(ex);
		}
		bind(HttpClientManager.class).to(DefaultHttpClientManager.class);
		bind(RedirectStrategy.class).to(RedirectAfterPostStrategy.class);
	}

	/**
	 * If the property {@code ssl.trust.all} equals {@code true}, a {@link TrustAllManager} is
	 * installed, i. e. all certificates are trusted, and host name verification is turned off.
	 * Otherwise, {@link LtSSLSocketFactory} is registered for HTTPS, if either a key store, a trust
	 * store or both are configured using the following properties:</p>
	 * <p>
	 * <ul>
	 * <li>{@code javax.net.ssl.keyStore}</li>
	 * <li>{@code javax.net.ssl.keyStorePassword}</li>
	 * <li>{@code javax.net.ssl.keyStoreType}</li>
	 * <li>{@code javax.net.ssl.trustStore}</li>
	 * <li>{@code javax.net.ssl.trustStorePassword}</li>
	 * <li>{@code javax.net.ssl.trustStoreType}</li>
	 * </ul>
	 * </p>
	 * <p>
	 * {@code javax.net.ssl.trustStore} and {@code javax.net.ssl.keyStore} must point to resources
	 * on the classpath.
	 * </p>
	 * 
	 * @param properties
	 *            the properties
	 * @return the {@link SchemeRegistry} the SchemeRegistry used for the HttpClient's
	 *         {@link ClientConnectionManager} registered for HTTPS
	 */
	@Provides
	@Singleton
	protected SchemeRegistry provideSchemeRegistry(final PropertiesMap properties) {
		SchemeRegistry registry = SchemeRegistryFactory.createDefault();

		if (properties.getBoolean(SSL_TRUST_ALL)) {
			try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[] { new TrustAllManager() }, null);
				SSLSocketFactory ssf = new SSLSocketFactory(ctx, new AllowAllHostnameVerifier());
				registry.register(new Scheme("https", 443, ssf));
			} catch (GeneralSecurityException ex) {
				Throwables.propagate(ex);
			}
		} else {
			String keyStore = trimToNull(properties.get(KEY_STORE));
			String trustStore = trimToNull(properties.get(TRUST_STORE));

			if (keyStore != null || trustStore != null) {
				String keyStorePassword = trimToNull(properties.get(KEY_STORE_PASSWORD));
				String keyStoreType = trimToNull(properties.get(KEY_STORE_TYPE));

				String trustStorePassword = trimToNull(properties.get(TRUST_STORE_PASSWORD));
				String trustStoreType = trimToNull(properties.get(TRUST_STORE_TYPE));

				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				URL keyStoreUrl = keyStore != null ? loader.getResource(keyStore) : null;
				URL trustStoreUrl = trustStore != null ? loader.getResource(trustStore) : null;

				LayeredSchemeSocketFactory socketFactory = new LtSSLSocketFactory(keyStoreUrl, keyStorePassword,
						keyStoreType, trustStoreUrl, trustStorePassword, trustStoreType);

				registry.register(new Scheme(HTTPS, 443, socketFactory));
			}
		}
		return registry;
	}

	/**
	 * Provides the HttpClient implementation.
	 * 
	 * @param httpParams
	 *            Parameters for the HttpClient. If {@code null}, defaults are used.
	 * @param connectionManager
	 *            The connection manager for the HttpClient. If {@code null}, the default is used.
	 * @return the {@link HttpClient} instance
	 */
	@Provides
	protected HttpClient provideHttpClient(final HttpParams httpParams, final ClientConnectionManager connectionManager,
			final RedirectStrategy redirectStrategy) {
		DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
		httpClient.setRedirectStrategy(redirectStrategy);
		return httpClient;

	}

	/**
	 * <p>
	 * Provides parameter for the HttpClient. This default implementation sets the following
	 * parameters:
	 * </p>
	 * <p>
	 * <ul>
	 * <li>http.protocol.content-charset: "UTF-8"</li>
	 * <li>http.useragent: "perfLoad <code>${perfLoadVersion}</code>"</li>
	 * <li>http.protocol.cookie-policy: "best-match"</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The default may be overridden using properties with the same names. Additionally, the user
	 * agent may also be set in the {@link PlaceholderContainer} in order to vary it during a test,
	 * the latter taking precendence over the properties.
	 * </p>
	 * 
	 * @param perfLoadVersion
	 *            the perfLoad version (used as part of the user agent string)
	 * @return the parameters
	 * @see CookiePolicy
	 */
	@Provides
	protected HttpParams provideHttpParams(@PerfLoadVersion final String perfLoadVersion, final InetAddress localAddress,
			final PropertiesMap properties, final PlaceholderContainer placeholderContainer) {
		HttpParams params = new BasicHttpParams();

		String charset = properties.get(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset);

		String cookiePolicy = properties.get(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		params.setParameter(ClientPNames.COOKIE_POLICY, cookiePolicy);

		params.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);

		String userAgent = placeholderContainer.get(CoreProtocolPNames.USER_AGENT);
		if (userAgent == null) {
			userAgent = properties.get(CoreProtocolPNames.USER_AGENT);
			if (userAgent == null) {
				userAgent = "perfLoad " + perfLoadVersion;
			}
		}
		params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);

		return params;
	}
}
