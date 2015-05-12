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

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.lang.annotation.Annotation;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mgmtp.perfload.core.client.config.annotations.PerfLoadVersion;
import com.mgmtp.perfload.core.client.config.scope.ExecutionScoped;
import com.mgmtp.perfload.core.client.web.config.AbstractWebLtModule;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.common.util.PropertiesMap;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

/**
 * Guice module for {@link OkHttpClient} configuration.
 *
 * @author rnaegele
 */
public class OkHttpModule extends AbstractWebLtModule {

	public OkHttpModule(final PropertiesMap testplanProperties) {
		super(testplanProperties);
	}

	@Override
	protected void doConfigureWebModule() {
		bind(OkHttpClient.class).toProvider(OkHttpClientProvider.class);
		bind(OkHttpRequestHandler.class);

		// Create linked bindings to the HttpRequestHandler for each HTTP method
		bindRequestHandler("GET").to(OkHttpRequestHandler.class);
		bindRequestHandler("POST").to(OkHttpRequestHandler.class);
		bindRequestHandler("PUT").to(OkHttpRequestHandler.class);
		bindRequestHandler("DELETE").to(OkHttpRequestHandler.class);
		bindRequestHandler("HEAD").to(OkHttpRequestHandler.class);
		bindRequestHandler("OPTIONS").to(OkHttpRequestHandler.class);
		bindRequestHandler("TRACE").to(OkHttpRequestHandler.class);
	}

	/**
	 * Creates a request builder setting perfLoad's own user agent header.
	 *
	 * @param perfLoadVersion
	 *            perfLoad's version
	 * @param properties
	 *            the properties map
	 * @return the request builder
	 */
	@Provides
	protected Request.Builder providerRequestBuilder(@PerfLoadVersion final String perfLoadVersion, final PropertiesMap properties) {
		Request.Builder requestBuilder = new Request.Builder();
		String userAgent = properties.get("http.useragent");
		if (userAgent == null) {
			userAgent = "perfLoad " + perfLoadVersion;
		}
		requestBuilder.addHeader("User-Agent", userAgent);
		return requestBuilder;
	}

	/**
	 * Creates a {@link CookieManager} instance with policy {@link CookiePolicy#ACCEPT_ALL
	 * ACCEPT_ALL}.
	 *
	 * @return the cookie manager
	 */
	@Provides
	@ExecutionScoped
	protected CookieHandler provideCookieHandler() {
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		return cookieManager;
	}

	/**
	 * <p>
	 * Creates a binding for the map of request handlers. The method looks up all bound request
	 * handlers and adds them to the map. Request handlers must be bound with an {@link Named}
	 * annotation. The annotation's value is used as the map key and reflects the request type.
	 * </p>
	 * <p>
	 * A custom GET request handler would e. g. end up in the returned map if it is bound as
	 * follows:
	 * </p>
	 * <p>
	 * {@code bindRequestHandler("GET").to(GetRequestHandler.class)}
	 * </p>
	 *
	 * @param injector
	 *            the Guice injector which is used to find {@link RequestHandler} bindings
	 * @return the map of request handlers
	 */
	@Provides
	@Singleton
	protected Map<String, RequestHandler> provideRequestHandlers(final Injector injector) {
		List<Binding<RequestHandler>> bindings = injector.findBindingsByType(TypeLiteral.get(RequestHandler.class));
		Map<String, RequestHandler> requestHandlers = newHashMapWithExpectedSize(bindings.size());
		for (Binding<RequestHandler> binding : bindings) {
			Key<RequestHandler> key = binding.getKey();
			Annotation annotation = key.getAnnotation();
			if (annotation instanceof Named) {
				String type = ((Named) annotation).value();
				RequestHandler requestHandler = binding.getProvider().get();
				requestHandlers.put(type, requestHandler);
			}
		}
		return ImmutableMap.copyOf(requestHandlers);
	}
}
