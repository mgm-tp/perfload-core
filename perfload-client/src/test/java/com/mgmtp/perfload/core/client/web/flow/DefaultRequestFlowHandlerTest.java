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
package com.mgmtp.perfload.core.client.web.flow;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.mockito.Matchers;
import org.mockito.internal.verification.VerificationModeFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.inject.util.Providers;
import com.mgmtp.perfload.core.client.util.ConstantWaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.client.web.WebErrorHandler;
import com.mgmtp.perfload.core.client.web.config.WebLtModule;
import com.mgmtp.perfload.core.client.web.event.DefaultLoggingListener;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.http.DefaultHttpClientManager;
import com.mgmtp.perfload.core.client.web.http.HttpClientManager;
import com.mgmtp.perfload.core.client.web.mock.MockRequestFlowListener;
import com.mgmtp.perfload.core.client.web.mock.MockRequestHandler;
import com.mgmtp.perfload.core.client.web.request.HttpRequestHandler;
import com.mgmtp.perfload.core.client.web.request.InvalidRequestHandlerException;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.DefaultDetailExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultHeaderExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultResponseValidator;
import com.mgmtp.perfload.core.client.web.template.DefaultTemplateTransformer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate.HeaderExtraction;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;
import com.mgmtp.perfload.logging.ResultLogger;
import com.mgmtp.perfload.logging.TimeInterval;

/**
 * @author rnaegele
 */
public class DefaultRequestFlowHandlerTest {

	private final Provider<HttpClientManager> httpClientManagerProvider = Providers
			.<HttpClientManager>of(new DefaultHttpClientManager(
					new Provider<HttpClient>() {
						@Override
						public HttpClient get() {
							HttpClient httpClient = mock(HttpClient.class);
							HttpResponse response = mock(HttpResponse.class);
							ClientConnectionManager connMgr = mock(ClientConnectionManager.class);

							when(httpClient.getConnectionManager()).thenReturn(connMgr);
							when(response.getStatusLine()).thenReturn(
									new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "ok"));
							when(response.getEntity()).thenReturn(new ByteArrayEntity(new byte[] { 42 }));
							when(response.getAllHeaders()).thenReturn(
									new Header[] {},
									new Header[] { new BasicHeader("foo", "bar") }
									);

							try {
								when(httpClient.execute(Matchers.<HttpUriRequest>any(), Matchers.<HttpContext>any()))
										.thenReturn(response);
							} catch (Exception ex) {
								throw new AssertionError(ex.getMessage());
							}
							return httpClient;
						}
					}, UUID.randomUUID(), "myOperation", WebLtModule.CONTENT_TYPE_PATTERNS));

	private final Provider<String> targetHostProvider = new Provider<String>() {
		@Override
		public String get() {
			return "http://localhost";
		}
	};

	@AfterMethod
	public void clearInterruptStatus() {
		Thread.interrupted();
	}

	@Test
	public void testNormalFlow() throws Exception {
		RequestTemplate getTemplate1 = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate getTemplate2 = new RequestTemplate("GET", "false", "/testuri?param=value", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate getTemplate3 = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of("param", "value"), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate getTemplate4 = new RequestTemplate("GET", "false", "/testuri?param1=value1", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of("param2", "value2"), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "http://localhost/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());

		List<RequestTemplate> templates = newArrayList(getTemplate1, getTemplate2, getTemplate3, getTemplate4, postTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		HttpRequestHandler requestHandler = new HttpRequestHandler(httpClientManagerProvider, targetHostProvider);
		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("GET", requestHandler, "POST",
				requestHandler);
		MockRequestFlowListener mockListener = new MockRequestFlowListener();

		final ResultLogger logger = mock(ResultLogger.class);
		DefaultLoggingListener loggingListener = new DefaultLoggingListener(new Provider<ResultLogger>() {
			@Override
			public ResultLogger get() {
				return logger;
			}
		});

		List<Pattern> pattern = asList(Pattern.compile("no_error_pattern"));

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows, requestHandlers,
				new DefaultTemplateTransformer(), new DefaultResponseValidator(Collections.<Integer>emptySet(),
						Collections.<Integer>emptySet(), pattern), new DefaultDetailExtractor(), new DefaultHeaderExtractor(),
				new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)), new DefaultPlaceholderContainer(),
				ImmutableSet.<RequestFlowEventListener>of(mockListener, loggingListener), new WebErrorHandler(),
				UUID.randomUUID());

		handler.execute();

		assertEquals(mockListener.getEventCalls(), 24);

		verify(logger, VerificationModeFactory.atLeastOnce()).logResult(Matchers.anyString(), Matchers.anyLong(),
				Matchers.<TimeInterval>any(), Matchers.<TimeInterval>any(), Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString(), Matchers.<UUID>any(), Matchers.<UUID>any());
	}

	@Test
	public void testInterrupt() throws Exception {
		RequestTemplate getTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "http://localhost/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());

		List<RequestTemplate> templates = newArrayList(getTemplate, postTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows,
				Collections.<String, RequestHandler>emptyMap(), null, null, null, null, new WaitingTimeManager(0L,
						new ConstantWaitingTimeStrategy(0L)), null, Collections.<RequestFlowEventListener>emptySet(),
				new WebErrorHandler(), UUID.randomUUID());

		Thread.currentThread().interrupt();
		try {
			handler.execute();
			fail("Expected AbortionException.");
		} catch (AbortionException ex) {
			assertEquals(ex.getStatus(), LtStatus.INTERRUPTED);
		}
	}

	@Test
	public void testInvalidRequestHandler() throws Exception {
		RequestTemplate getTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());

		List<RequestTemplate> templates = newArrayList(getTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		HttpRequestHandler requestHandler = new HttpRequestHandler(httpClientManagerProvider, targetHostProvider);
		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("POST", requestHandler);

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows, requestHandlers,
				new DefaultTemplateTransformer(), null, null, null, new WaitingTimeManager(0L,
						new ConstantWaitingTimeStrategy(0L)), null, Collections.<RequestFlowEventListener>emptySet(),
				new WebErrorHandler(), UUID.randomUUID());

		try {
			handler.execute();
			fail("Expected AbortionException.");
		} catch (AbortionException ex) {
			assertEquals(ex.getMessage(), "No request handler for type 'GET' available.");
			assertTrue(ex.getCause() instanceof InvalidRequestHandlerException);
		}
	}

	@Test
	public void testInvalidResponse() throws Exception {
		RequestTemplate getTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "http://localhost/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());

		List<RequestTemplate> templates = newArrayList(getTemplate, postTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("GET", new MockRequestHandler(404));
		MockRequestFlowListener listener = new MockRequestFlowListener();

		List<Pattern> pattern = asList(Pattern.compile("no_error_pattern"));

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows, requestHandlers,
				new DefaultTemplateTransformer(), new DefaultResponseValidator(Collections.<Integer>emptySet(),
						ImmutableSet.<Integer>of(404), pattern), null, null, new WaitingTimeManager(0L,
						new ConstantWaitingTimeStrategy(0L)), new DefaultPlaceholderContainer(),
				ImmutableSet.<RequestFlowEventListener>of(listener), new WebErrorHandler(), UUID.randomUUID());

		handler.execute();

		assertEquals(listener.getEventCalls(), 4); // 1 flow x 1 template -> 4 events, 2nd template is not executed
	}

	@Test
	public void testSkippedRequest() throws Exception {
		RequestTemplate skippedTemplate = new RequestTemplate("GET", "true", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());
		RequestTemplate executedTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList());

		List<RequestTemplate> templates = newArrayList(skippedTemplate, executedTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("GET", new MockRequestHandler(404));
		MockRequestFlowListener listener = new MockRequestFlowListener();

		List<Pattern> pattern = asList(Pattern.compile("no_error_pattern"));

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows, requestHandlers,
				new DefaultTemplateTransformer(), new DefaultResponseValidator(Collections.<Integer>emptySet(),
						ImmutableSet.<Integer>of(404), pattern), new DefaultDetailExtractor(), new DefaultHeaderExtractor(),
				new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)), new DefaultPlaceholderContainer(),
				ImmutableSet.<RequestFlowEventListener>of(listener), new WebErrorHandler(), UUID.randomUUID());

		handler.execute();

		assertEquals(listener.getEventCalls(), 6); // 1 flow x 2 template -> 6 events
	}
}
