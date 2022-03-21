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
package com.mgmtp.perfload.core.client.web.flow;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.inject.Provider;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.mgmtp.perfload.core.client.util.ConstantWaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.client.web.WebErrorHandler;
import com.mgmtp.perfload.core.client.web.event.DefaultLoggingListener;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.mock.MockRequestFlowListener;
import com.mgmtp.perfload.core.client.web.mock.MockRequestHandler;
import com.mgmtp.perfload.core.client.web.okhttp.OkHttpManager;
import com.mgmtp.perfload.core.client.web.okhttp.OkHttpRequestHandler;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;


/**
 * @author rnaegele
 */
public class DefaultRequestFlowHandlerTest {

	//	private final Provider<AsyncHttpClient> ahcProvider = () -> new AsyncHttpClient();
	//	private final Provider<CookieHandler> cookieHandlerProvider = () -> new CookieManager();
	//	private final Provider<AhcManager> ahcManagerProvider = () -> new DefaultAhcManager(ahcProvider, new HashMap<>(), cookieHandlerProvider,
	//			UUID.randomUUID());
	private final Provider<UUID> uuidProvider = () -> UUID.randomUUID();
	private final Provider<String> operationProvider = () -> "myOperation";
	private final Provider<OkHttpManager> okHttpManagerProvider = () -> new OkHttpManager(() -> new OkHttpClient());

	@AfterMethod
	public void clearInterruptStatus() {
		Thread.interrupted();
	}

	@Test
	public void testNormalFlow() throws Exception {
		RequestTemplate getTemplate1 = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		RequestTemplate getTemplate2 = new RequestTemplate("GET", "false", "/testuri?param=value", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		RequestTemplate getTemplate3 = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of("param", "value"), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		RequestTemplate getTemplate4 = new RequestTemplate("GET", "false", "/testuri?param1=value1", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of("param2", "value2"), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of("param", "value"), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		MockWebServer server = new MockWebServer();
		MockResponse mockResponse = new MockResponse()
				.addHeader("foo", "bar")
				.setResponseCode(200)
				.setBody("ok");

		IntStream.range(0, 10).forEach(i -> server.enqueue(mockResponse));
		server.start();
		Provider<String> targetHostProvider = () -> server.url("").toString();
		Provider<Request.Builder> requestBuilderProvider = () -> new Request.Builder();

		List<RequestTemplate> templates = newArrayList(getTemplate1, getTemplate2, getTemplate3, getTemplate4, postTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		RequestHandler requestHandler = new OkHttpRequestHandler(okHttpManagerProvider, targetHostProvider, uuidProvider,
				operationProvider, requestBuilderProvider);
		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("GET", requestHandler, "POST", requestHandler);
		MockRequestFlowListener mockListener = new MockRequestFlowListener();

		final ResultLogger logger = mock(ResultLogger.class);
		DefaultLoggingListener loggingListener = new DefaultLoggingListener(() -> logger);

		List<Pattern> pattern = asList(Pattern.compile("no_error_pattern"));

		DefaultRequestFlowHandler handler = new DefaultRequestFlowHandler(requestFlows, requestHandlers,
				new DefaultTemplateTransformer(), new DefaultResponseValidator(Collections.<Integer>emptySet(),
						Collections.<Integer>emptySet(), pattern), new DefaultDetailExtractor(), new DefaultHeaderExtractor(),
						new WaitingTimeManager(0L, new ConstantWaitingTimeStrategy(0L)), new DefaultPlaceholderContainer(),
						ImmutableSet.<RequestFlowEventListener>of(mockListener, loggingListener), new WebErrorHandler(),
						UUID.randomUUID());

		handler.execute();

		assertEquals(mockListener.getEventCalls(), 24);
	}

	@Test
	public void testInterrupt() throws Exception {
		RequestTemplate getTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");
		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "http://localhost/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

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
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

		List<RequestTemplate> templates = newArrayList(getTemplate);
		RequestFlow flow = new RequestFlow("flow.xml", templates);
		List<RequestFlow> requestFlows = newArrayList(flow, flow);

		Map<String, RequestHandler> requestHandlers = ImmutableMap.<String, RequestHandler>of("POST", (template, requestId) -> null);

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
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");
		RequestTemplate postTemplate = new RequestTemplate("POST", "false", "http://localhost/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

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
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");
		RequestTemplate executedTemplate = new RequestTemplate("GET", "false", "/testuri", null,
				ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
				Collections.<HeaderExtraction>emptyList(), Collections.<DetailExtraction>emptyList(), "true");

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
