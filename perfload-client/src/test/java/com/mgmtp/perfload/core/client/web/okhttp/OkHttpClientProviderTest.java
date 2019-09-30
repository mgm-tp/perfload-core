package com.mgmtp.perfload.core.client.web.okhttp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.mgmtp.perfload.core.client.util.ConstantWaitingTimeStrategy;
import com.mgmtp.perfload.core.client.util.DefaultPlaceholderContainer;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.client.web.WebErrorHandler;
import com.mgmtp.perfload.core.client.web.event.DefaultLoggingListener;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.flow.DefaultRequestFlowHandler;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.mock.MockRequestFlowListener;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.DefaultDetailExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultHeaderExtractor;
import com.mgmtp.perfload.core.client.web.response.DefaultResponseValidator;
import com.mgmtp.perfload.core.client.web.template.DefaultTemplateTransformer;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.logging.ResultLogger;
import com.squareup.okhttp.OkHttpClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.Request;

import javax.inject.Provider;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Testing the followRedirect parameter (which is configurable for each test)
 *
 * Created by amueller on 23.09.2019.
 */
public class OkHttpClientProviderTest {
    private final Provider<UUID> uuidProvider = () -> UUID.randomUUID();
    private final Provider<String> operationProvider = () -> "myOperation";

    @Test
    public void testDontFollowRedirects() throws Exception {
        // request flow contains 2 templates, the first's 302 response shpuld be accepted and ignored
        testIntern(false,2);
    }

    @Test
    public void testFollowRedirects() throws Exception {
        // request flow contains 2 templates, but the first redirected one should lead to 3 requests
        testIntern(true,3);
    }

    private void testIntern(boolean followRedirects, int numRequests) throws Exception {
        // Templates for 2 requests
        RequestTemplate getTemplate = new RequestTemplate("GET", "false", "/testuri1", null,
                ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
                Collections.<RequestTemplate.HeaderExtraction>emptyList(), Collections.<RequestTemplate.DetailExtraction>emptyList(), "true");
        RequestTemplate getTemplate2 = new RequestTemplate("GET", "false", "/testuri2", null,
                ImmutableSetMultimap.<String, String>of(), ImmutableSetMultimap.<String, String>of(), null,
                Collections.<RequestTemplate.HeaderExtraction>emptyList(), Collections.<RequestTemplate.DetailExtraction>emptyList(), "true");

        MockWebServer server = new MockWebServer();
        // Mock server returns a 302 to the first requests
        MockResponse mockResponse = new MockResponse()
                .addHeader("Location", "/testuri1b")
                .setResponseCode(302)
                .setBody("ok");
        // returns a 200 to the second and third (if any) request
        MockResponse mockResponse2 = new MockResponse()
                .setResponseCode(200)
                .setBody("ok");
        MockResponse mockResponse3 = new MockResponse()
                .setResponseCode(200)
                .setBody("ok");
        server.enqueue(mockResponse);
        server.enqueue(mockResponse2);
        server.enqueue(mockResponse3);
        server.start();
        Provider<String> targetHostProvider = () -> server.getUrl("").toString();
        Provider<Request.Builder> requestBuilderProvider = () -> new Request.Builder();

        List<RequestTemplate> templates = newArrayList(getTemplate, getTemplate2);
        RequestFlow flow = new RequestFlow("flow.xml", templates);
        List<RequestFlow> requestFlows = new ArrayList<RequestFlow>(1);
        requestFlows.add(flow);

        Provider<OkHttpManager> okHttpManagerProvider = () -> new OkHttpManager(() -> getOkHttpClient(followRedirects));
        RequestHandler requestHandler = new OkHttpRequestHandler( okHttpManagerProvider, targetHostProvider, uuidProvider, operationProvider, requestBuilderProvider);
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
        // number of events is the same in both cases
        assertEquals(mockListener.getEventCalls(), 6);
        // but following redirects should increase the number of reqeusts by 1
        assertEquals(server.getRequestCount(), numRequests);
    }

    // build the client by hand because it is hard to inject the followRedirect parameter
    private static OkHttpClient getOkHttpClient(boolean followRedirects) {
        OkHttpClient cl = new OkHttpClient();
        cl.setFollowRedirects(followRedirects);
        cl.setFollowSslRedirects(followRedirects);
        return cl;
    }
}