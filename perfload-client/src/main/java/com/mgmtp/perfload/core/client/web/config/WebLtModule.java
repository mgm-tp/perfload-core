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
package com.mgmtp.perfload.core.client.web.config;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mgmtp.perfload.core.client.config.DriverSelectionPredicate;
import com.mgmtp.perfload.core.client.config.annotations.Operation;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.web.WebErrorHandler;
import com.mgmtp.perfload.core.client.web.WebLtDriver;
import com.mgmtp.perfload.core.client.web.config.annotations.AllowedStatusCodes;
import com.mgmtp.perfload.core.client.web.config.annotations.ContentTypePatterns;
import com.mgmtp.perfload.core.client.web.config.annotations.ErrorPatterns;
import com.mgmtp.perfload.core.client.web.config.annotations.ForbiddenStatusCodes;
import com.mgmtp.perfload.core.client.web.config.annotations.HttpClientManagement;
import com.mgmtp.perfload.core.client.web.config.annotations.LoggingListener;
import com.mgmtp.perfload.core.client.web.event.DefaultLoggingListener;
import com.mgmtp.perfload.core.client.web.event.HttpClientManagementListener;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.flow.DefaultRequestFlowHandler;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.flow.RequestFlowHandler;
import com.mgmtp.perfload.core.client.web.io.XmlRequestFlowReader;
import com.mgmtp.perfload.core.client.web.request.HttpRequestHandler;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.DefaultDetailExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultHeaderExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultResponseValidator;
import com.mgmtp.perfload.core.client.web.response.DetailExtractor;
import com.mgmtp.perfload.core.client.web.response.HeaderExtractor;
import com.mgmtp.perfload.core.client.web.response.ResponseValidator;
import com.mgmtp.perfload.core.client.web.template.DefaultTemplateTransformer;
import com.mgmtp.perfload.core.client.web.template.TemplateTransformer;
import com.mgmtp.perfload.core.common.util.PropertiesMap;

/**
 * Guice module for binding Web-specific classes.
 * 
 * @author rnaegele
 */
public class WebLtModule extends AbstractWebLtModule {
	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final List<Pattern> CONTENT_TYPE_PATTERNS = ImmutableList.of(
			Pattern.compile("text/.*"),
			Pattern.compile("application/xml"),
			Pattern.compile("application/json")
			);

	private final ConcurrentMap<String, List<RequestFlow>> requestFlowCache = new MapMaker().initialCapacity(3).makeMap();

	public WebLtModule(final PropertiesMap testplanProperties) {
		super(testplanProperties);
	}

	@Override
	protected void doConfigureWebModule() {
		bind(RequestFlowHandler.class).to(DefaultRequestFlowHandler.class);

		// Create an extra binding for the HttpRequestHandler, so handlers for individual methods can be linked to it
		bind(HttpRequestHandler.class);

		// Create linked bindings to the HttpRequestHandler for each HTTP method
		bindRequestHandler("GET").to(HttpRequestHandler.class);
		bindRequestHandler("POST").to(HttpRequestHandler.class);
		bindRequestHandler("PUT").to(HttpRequestHandler.class);
		bindRequestHandler("DELETE").to(HttpRequestHandler.class);
		bindRequestHandler("HEAD").to(HttpRequestHandler.class);
		bindRequestHandler("OPTIONS").to(HttpRequestHandler.class);
		bindRequestHandler("TRACE").to(HttpRequestHandler.class);

		bind(TemplateTransformer.class).to(DefaultTemplateTransformer.class);
		bind(ResponseValidator.class).to(DefaultResponseValidator.class);
		bind(DetailExtractor.class).to(DefaultDetailExtractor.class);
		bind(HeaderExtractor.class).to(DefaultHeaderExtractor.class);
		bind(ErrorHandler.class).to(WebErrorHandler.class);

		// Create an extra binding for the HttpClientManagementListener using a qualifying annotation.
		// The Multibinder for the event listener will then link to this binding so it may be overridden.
		// This is necessary because multibindings themselves cannot be overridden.
		bind(LtRunnerEventListener.class).annotatedWith(HttpClientManagement.class).to(HttpClientManagementListener.class);

		// Bind HttpClientManagementListener with a key as just registered, so it can be overridden
		bindLtRunnerEventListener().to(Key.get(LtRunnerEventListener.class, HttpClientManagement.class));

		// Create an extra binding for the DefaultLoggingListener using a qualifying annotation.
		// The Multibinder for the event listener will then link to this binding so it may be overridden.
		// This is necessary because multibindings themselves cannot be overridden.
		bind(RequestFlowEventListener.class).annotatedWith(LoggingListener.class).to(DefaultLoggingListener.class);

		// Bind DefaultLoggingListener with a key as just registered, so it can be overridden
		bindRequestFlowEventListener().to(Key.get(RequestFlowEventListener.class, LoggingListener.class));

		install(new HttpClientManagerModule());

		bindLtDriver("web").forPredicate(new DriverSelectionPredicate() {
			@Override
			public boolean apply(final String operation, final PropertiesMap properties) {
				return properties.containsKey("operation." + operation + ".requestflows");
			}
		}).to(WebLtDriver.class);
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

	/**
	 * <p>
	 * Creates a binding for a list of regular expression patterns used to identify erronous HTTP
	 * responses.
	 * </p>
	 * <p>
	 * Patterns are read from properties with the following keys
	 * {@code responseParser.errorPattern.<index>} (the indices being one-based consecutive
	 * integers).
	 * </p>
	 * <p>
	 * Example:<br />
	 * {@code responseParser.errorPattern.1=<!--Lasttest stop -->}<br />
	 * {@code responseParser.errorPattern.2=(?is)^((?!</html>).)*$}
	 * </p>
	 * 
	 * @param properties
	 *            the properties
	 * @return a list of {@link Pattern} objects
	 */
	@Provides
	@Singleton
	@ErrorPatterns
	protected List<Pattern> provideErrorPatterns(final PropertiesMap properties) {
		return readPatternsFromProps(properties, "responseParser.errorPattern.");
	}

	/**
	 * <p>
	 * Creates a binding for a list of regular expression patterns used to identify HTTP response
	 * bodies as text.
	 * </p>
	 * <p>
	 * Patterns are read from properties with the following keys
	 * {@code responseParser.contentTypePattern.<index>} (the indices being one-based consecutive
	 * integers).
	 * </p>
	 * <p>
	 * Example:<br />
	 * {@code responseParser.contentTypePattern.1=text/.*}<br />
	 * {@code responseParser.contentTypePattern.2=application/json}
	 * </p>
	 * <p>
	 * Defaults to the following patterns if nothing is configured:
	 * <ul>
	 * <li>text/.*</li>
	 * <li>application/json</li>
	 * <li>application/xml</li>
	 * </ul>
	 * </p>
	 * 
	 * @param properties
	 *            the properties
	 * @return a list of {@link Pattern} objects
	 */
	@Provides
	@Singleton
	@ContentTypePatterns
	protected List<Pattern> provideContentTypePatterns(final PropertiesMap properties) {
		List<Pattern> list = readPatternsFromProps(properties, "responseParser.contentTypePattern.");
		if (list.isEmpty()) {
			return CONTENT_TYPE_PATTERNS;
		}

		return list;
	}

	private List<Pattern> readPatternsFromProps(final PropertiesMap properties, final String keyPrefix) {
		List<Pattern> patterns = newArrayList();

		for (int i = 1;; ++i) {
			String pattern = properties.get(keyPrefix + i);
			if (pattern == null) {
				break;
			}
			patterns.add(Pattern.compile(pattern));
		}

		return ImmutableList.copyOf(patterns);
	}

	/**
	 * <p>
	 * Creates bindings for the request flows. Multiple request flows may be configured and will be
	 * returned as a list. Request flows are associated with an {@code operation} and thus
	 * configured per {@code operation} as follows:
	 * </p>
	 * <p>
	 * {@code operation.myOperation1.requestflows=foo_request_flow_1.xml,foo_request_flow_2.xml}<br />
	 * {@code operation.myOperation2.requestflows=bar_request_flow.xml}
	 * </p>
	 * <p>
	 * Optionally, a path to a directory in the classpath may be defined with the property
	 * {@code requestfolws.path}.
	 * </p>
	 * <p>
	 * Multiple request flow files per {@code operation} must be comma-separated.
	 * </p>
	 * 
	 * @param operation
	 *            the operation
	 * @param properties
	 *            the properties
	 * @return a list of {@link RequestFlow} objects
	 */
	@Provides
	protected List<RequestFlow> provideRequestFlows(@Operation final String operation, final PropertiesMap properties) {
		try {
			String templates = properties.get("operation." + operation + ".requestflows");
			String path = trimToEmpty(properties.get("requestflows.path"));
			if (path.length() > 0 && !path.endsWith("/")) {
				path += "/";
			}

			// Templates are cached in order to save heap memory
			List<RequestFlow> requestFlows = requestFlowCache.get(templates);
			if (requestFlows == null) {
				// It might happen that multiple threads do the read at the same time,
				// but on the other hand, we don't have to synchronize. In the end only one read wins anyways.

				List<String> templateResources = asList(templates.split("\\s*,\\s*"));

				List<RequestFlow> newRequestFlows = newArrayListWithCapacity(templateResources.size());
				for (String templateResource : templateResources) {
					XmlRequestFlowReader reader = new XmlRequestFlowReader(path, templateResource);
					RequestFlow flow = reader.readFlow();
					newRequestFlows.add(flow);
				}

				// We store immutable lists
				newRequestFlows = ImmutableList.copyOf(newRequestFlows);

				requestFlows = requestFlowCache.putIfAbsent(templates, newRequestFlows);
				// If non-null, another thread was faster
				if (requestFlows == null) {
					requestFlows = newRequestFlows;
				}
			}

			log.debug("Providing request flows for operation '{}': {}", operation, requestFlows);
			return requestFlows;
		} catch (Exception ex) {
			throw new IllegalStateException("Error providing list of request flows.", ex);
		}
	}

	/**
	 * Provides a set of allowed status codes.
	 * 
	 * @param properties
	 *            the properties
	 * @return the set of status code; may be empty
	 */
	@Provides
	@Singleton
	@AllowedStatusCodes
	protected Set<Integer> provideAllowedStatusCodes(final PropertiesMap properties) {
		String value = properties.get("responseParser.allowedStatusCodes");

		if (StringUtils.isNotBlank(value)) {
			String[] split = value.split("\\s*,\\s*");
			Set<Integer> result = newHashSetWithExpectedSize(split.length);
			for (String s : split) {
				result.add(Integer.valueOf(s));
			}
			return result;
		}

		return Collections.emptySet();
	}

	/**
	 * Provides a set of forbidden status codes.
	 * 
	 * @param properties
	 *            the properties
	 * @return the set of status code; may be empty
	 */
	@Provides
	@Singleton
	@ForbiddenStatusCodes
	protected Set<Integer> provideForbiddenStatusCodes(final PropertiesMap properties) {
		String value = properties.get("responseParser.forbiddenStatusCodes");

		if (StringUtils.isNotBlank(value)) {
			String[] split = value.split("\\s*,\\s*");
			Set<Integer> result = newHashSetWithExpectedSize(split.length);
			for (String s : split) {
				result.add(Integer.valueOf(s));
			}
			return result;
		}

		return Collections.emptySet();
	}
}
