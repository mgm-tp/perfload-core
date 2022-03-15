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

import java.net.InetAddress;

import javax.inject.Named;
import javax.inject.Provider;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import com.google.inject.Inject;
import com.mgmtp.perfload.core.client.web.net.LocalAddressSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.*;

import okhttp3.tls.HandshakeCertificates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JSR 330 provider for {@link OkHttpClient} instances.
 *
 * @author rnaegele
 */
public class OkHttpClientProvider implements Provider<OkHttpClient> {

    private static final Logger LOG = LoggerFactory.getLogger(OkHttpClientProvider.class);

    private Dispatcher dispatcher;

    private final Provider<InetAddress> localAddressProvider;
    // redirect behaviour has been made configurable
    private boolean followRedirects = true; // old setting still is default behaviour
    private static int redirectsLogged = 0; // log the configured choice exactly once
    private Protocol protocol_http_11 = Protocol.HTTP_1_1;
    private boolean useHTTP20 = false;
    private static int http20UseLogged = 0;

    private List<String> insecureHostsList = new ArrayList<>();
    private boolean dumpCookiesFlag = false;
    private OkHttpClient client=null;

    /**
     * Sets an optional list of hosts, which are set as unsecure hosts, checking
     * of certificates is disabled for those hosts
     *
     * @param insecureHosts If present value is taken from testplan.xml where it
     * may be configured in the following way:
     * <properties>
     * ...
     * <property name="insecureHosts">localhost,a.b.com</property>
     * </properties>
     */
    @Inject(optional = true)
    public void setInsecureHosts(@Named("insecureHosts") final String insecureHosts) {
        String[] insecureHostsArray = insecureHosts.split(",");
        for (String insecureHost : insecureHostsArray) {
            insecureHostsList.add(insecureHost.trim());
        }
    }

    /**
     * Activates dumping of cookies
     *
     * @param dumpCookies If present value is taken from testplan.xml where it
     * may be configured in the following way:
     * <properties>
     * ...
     * <property name="dumpCookies">true</property>
     * </properties>
     */
    @Inject(optional = true)
    public void setDumpCookies(@Named("dumpCookies") final String dumpCookies) {
        dumpCookiesFlag = Boolean.parseBoolean(dumpCookies.trim());
    }

    /**
     * Sets an optional boolean whether to follow redirects (default) or not
     *
     * @param followRedirects If present value is taken from testplan.xml where
     * it may be configured in the following way:
     * <properties>
     * ...
     * <property name="followRedirects">false</property>
     * </properties>
     */
    @Inject(optional = true)
    public void setFollowRedirects(@Named("followRedirects") final String followRedirects) {
        if (redirectsLogged++ < 5) {
            LoggerFactory.getLogger(getClass()).warn("test runs with followRedirects=" + followRedirects);
        }
        this.followRedirects = Boolean.valueOf(followRedirects);
    }

    /**
     * Sets an optional boolean whether to attempt to use HTTP 2.0 (default: false)
     *
     * @param useHTTP20 If present value is taken from testplan.xml where
     * it may be configured in the following way:
     * <properties>
     * ...
     * <property name="useHTTP20">true</property>
     * </properties>
     */
    @Inject(optional = true)
    public void setUseHTTP20(@Named("useHTTP20") final String useHTTP20) {
        this.useHTTP20 = Boolean.valueOf(useHTTP20);
        if (http20UseLogged++ < 5) {
            LoggerFactory.getLogger(getClass()).warn("test tries to use HTTP 2.0: " + (this.useHTTP20?"yes":"no"));
        }
    }

    /**
     * Set an optional {@link Dispatcher} for better control of asynchronous
     * requests.
     *
     * @param dispatcher the dispatcher
     */
    @Inject(optional = true)
    public void setDispatcher(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @param localAddressProvider the local address provider
     */
    @Inject
    protected OkHttpClientProvider(final Provider<InetAddress> localAddressProvider) {
        this.localAddressProvider = localAddressProvider;
    }

    /**
     * Creates a new OkHttpClient instance.
     *
     * @return the OkHttpClient
     */
    @Override
    public OkHttpClient get() {
        if (client==null) {
            boolean withProxy = false;
            OkHttpClient.Builder builder = new OkHttpClient.Builder().followRedirects(followRedirects).followSslRedirects(followRedirects);
            List<Protocol> protocols = new ArrayList<Protocol>(2);
            protocols.add(protocol_http_11);
            if (useHTTP20)
                protocols.add(Protocol.HTTP_2);
            builder = builder.protocols(protocols);
            builder.retryOnConnectionFailure(true);
            builder = builder.connectTimeout(180, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS);
            if (withProxy) {
                Proxy localProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8083));
                builder = builder.proxy(localProxy);
            }
            builder.cookieJar(new MyCookieJar(dumpCookiesFlag));

            if (insecureHostsList.size() > 0) {
                LOG.warn("Using HandshakeCertificate.Builder for ssl factory");
                HandshakeCertificates.Builder handshakeBuilder = new HandshakeCertificates.Builder();
                handshakeBuilder = handshakeBuilder.addPlatformTrustedCertificates();
                for (String insecureHost : insecureHostsList) {
                    LOG.warn("Adding insecure host '" + insecureHost + "'");
                    handshakeBuilder.addInsecureHost(insecureHost);
                }
                HandshakeCertificates clientCertificates = handshakeBuilder.build();
                builder.sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager());
            } else {
                LOG.warn("Using ssl context and TrustAllManager for ssl factory");
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{new TrustAllManager()}, null);
                    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                    builder.sslSocketFactory(sslSocketFactory, new TrustAllManager());
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    LOG.error("Could not create SocketFactory due to " + e);
                }
            }
            builder.socketFactory(new LocalAddressSocketFactory(SocketFactory.getDefault(), localAddressProvider));
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            if (dispatcher != null) {
                builder.dispatcher(dispatcher);
            }
            client = builder.build();
        }
        return client;
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
            return new java.security.cert.X509Certificate[]{};
        }
    }

    static class MyCookieJar implements CookieJar {

        private List<Cookie> cookies;
        private boolean dumpCookies;

        MyCookieJar(boolean dumpCookies) {
            this.dumpCookies = dumpCookies;
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            this.cookies = cookies;
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            if (dumpCookies) {
                int nCookies = cookies == null ? 0 : cookies.size();
                if (nCookies > 0) {
                    LOG.info("Loading following " + nCookies + " cookies for use in next request");
                    for (Cookie cookie : cookies) {
                        LOG.info(cookie.name() + " : " + cookie.value());
                    }
                } else {
                    LOG.info("No cookies for use in next request");
                }
            }
            if (cookies != null) {
                return cookies;
            }
            return new ArrayList<Cookie>();
        }
    }
}
