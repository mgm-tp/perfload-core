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

import static com.mgmtp.perfload.core.common.util.LtUtils.checkInterrupt;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.config.annotations.ExecutionId;
import com.mgmtp.perfload.core.client.config.scope.ExecutionScoped;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
import com.mgmtp.perfload.core.client.util.WaitingTimeManager;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEvent;
import com.mgmtp.perfload.core.client.web.event.RequestFlowEventListener;
import com.mgmtp.perfload.core.client.web.request.InvalidRequestHandlerException;
import com.mgmtp.perfload.core.client.web.request.RequestHandler;
import com.mgmtp.perfload.core.client.web.response.DetailExtractor;
import com.mgmtp.perfload.core.client.web.response.HeaderExtractor;
import com.mgmtp.perfload.core.client.web.response.ResponseInfo;
import com.mgmtp.perfload.core.client.web.response.ResponseValidator;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;
import com.mgmtp.perfload.core.client.web.template.TemplateTransformer;

/**
 * Default implementation of a {@link RequestFlowHandler}. It handles the complete logic for a run
 * of a Web load test and fires {@link RequestFlowEvent}s before and after request flows and before
 * and after requests. The "after" events are fired in {@code finally} blocks so that they are also
 * triggered in case of an exception. The exception will then be available through the
 * {@link RequestFlowEvent}.
 *
 * @author rnaegele
 */
@ExecutionScoped
public final class DefaultRequestFlowHandler implements RequestFlowHandler {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, RequestHandler> requestHandlers;
	private final List<RequestFlow> requestFlows;
	private final TemplateTransformer templateTransformer;
	private final ResponseValidator responseValidator;
	private final DetailExtractor detailExtractor;
	private final HeaderExtractor headerExtractor;
	private final WaitingTimeManager waitingTimeManager;
	private final PlaceholderContainer placeholderContainer;
	private final Set<RequestFlowEventListener> listeners;
	private final ErrorHandler errorHandler;
	private final UUID executionId;

	/**
	 * Constructs a new instance.
	 *
	 * @param requestFlows
	 *            the list of {@link RequestFlow}s to be processed
	 * @param requestHandlers
	 *            a map of {@link RequestHandler}s; must contain a request handler for each type of
	 *            request in the request flow
	 * @param templateTransformer
	 *            the {@link TemplateTransformer} used to make request template executable
	 * @param responseValidator
	 *            the {@link ResponseValidator} for validating the HTTP reponses
	 * @param detailExtractor
	 *            the {@link DetailExtractor} for extracting details from the reponse bodies
	 * @param headerExtractor
	 *            the {@link HeaderExtractor} for extracting headers from the reponses
	 * @param waitingTimeManager
	 *            the {@link WaitingTimeManager} that introduces a waiting time as configured before
	 *            each request
	 * @param placeholderContainer
	 *            the {@link PlaceholderContainer}
	 * @param listeners
	 *            a set of {@link RequestFlowEventListener}s
	 * @param errorHandler
	 *            the error handler which determines whether and exception should lead to the
	 *            abortion of the whole test
	 * @param executionId
	 *            the execution id
	 */
	@Inject
	DefaultRequestFlowHandler(final List<RequestFlow> requestFlows, final Map<String, RequestHandler> requestHandlers,
			final TemplateTransformer templateTransformer, final ResponseValidator responseValidator,
			final DetailExtractor detailExtractor, final HeaderExtractor headerExtractor,
			final WaitingTimeManager waitingTimeManager, final PlaceholderContainer placeholderContainer,
			final Set<RequestFlowEventListener> listeners, final ErrorHandler errorHandler, @ExecutionId final UUID executionId) {
		this.requestFlows = requestFlows;
		this.requestHandlers = requestHandlers;
		this.templateTransformer = templateTransformer;
		this.responseValidator = responseValidator;
		this.detailExtractor = detailExtractor;
		this.headerExtractor = headerExtractor;
		this.waitingTimeManager = waitingTimeManager;
		this.placeholderContainer = placeholderContainer;
		this.listeners = listeners;
		this.errorHandler = errorHandler;
		this.executionId = executionId;
	}

	@Override
	public void execute() throws Exception {
		Exception exception = null;

		for (ListIterator<RequestFlow> it = requestFlows.listIterator(); it.hasNext();) {
			RequestFlow requestFlow = it.next();
			// 1-based index, so we use "nextIndex()"
			int flowIndex = it.nextIndex();

			try {
				// fire event
				fireBeforeRequestFlow(flowIndex);

				// process requests
				for (final RequestTemplate template : requestFlow) {

					RequestTemplate executableTemplate = null;
					ResponseInfo responseInfo = null;
					UUID requestId = UUID.randomUUID();
					try {
						waitingTimeManager.sleepBeforeRequest();

						// check for interrupt and abort if necessary
						checkInterrupt();

						// fire event, also called for skipped requests, because event handler may decide whether to skip
						// must be called before the template is made executable, so parameters
						// can be put into the placeholder container
						fireBeforeRequest(flowIndex, template);

						executableTemplate = templateTransformer.makeExecutable(template, placeholderContainer);
						if (executableTemplate.isSkipped()) {
							log.info("Skipping request: {}", executableTemplate);
							continue;
						}

						log.info("Executing request: {}", executableTemplate);

						// look up request handler for the request's type
						String type = template.getType();
						RequestHandler handler = requestHandlers.get(type);
						if (handler == null) {
							throw new InvalidRequestHandlerException(String.format("No request handler for type '%s' available.",
									type));
						}

						responseInfo = handler.execute(executableTemplate, requestId);
						if (responseInfo != null) {
							log.debug(responseInfo.toString());

							// process response
							if (executableTemplate.isValidateResponse()) {
								responseValidator.validate(responseInfo);
							}
							detailExtractor.extractDetails(responseInfo, executableTemplate.getDetailExtractions(),
									placeholderContainer);
							headerExtractor.extractHeaders(responseInfo, executableTemplate.getHeaderExtractions(),
									placeholderContainer);
						}
					} catch (Exception ex) {
						exception = ex;

						// Handle error. Depending on the exception, the error handler may choose to abort the test.
						errorHandler.execute(ex);

						if (responseInfo != null) {
							// In case of an error we additionally log the response info at warn level
							log.warn(responseInfo.toString());
						}

						// In any case, we don't want to execute the remainder of
						// the current request flow if an exception occurred and break out of the loop.
						break;
					} finally {
						RequestTemplate template4Event = executableTemplate != null ? executableTemplate : template;
						// ResponseInfo is null the request is skipped of if an exception occurs when the HttpClient executes
						// the request. In order to make sure that we still get an entry in the measuring log in the case of and
						// exceptiohn, we need to create one. However, it must remain null, when the request is skipped in
						// order to avoid an entry in the measuring log.
						if (responseInfo == null && executableTemplate != null && !executableTemplate.isSkipped()) {
							responseInfo = new ResponseInfo.Builder()
									.methodType(template4Event.getType())
									.uri(template4Event.getUri())
									.uriAlias(template4Event.getUriAlias())
									.timestamp(System.currentTimeMillis())
									.executionId(executionId)
									.requestId(requestId)
									.build();
						}

						// always fire event, including skipped requests
						fireAfterRequest(flowIndex, template4Event, exception, responseInfo);
					}
				}
			} catch (Exception ex) {
				exception = ex;
				errorHandler.execute(ex);
			} finally {

				// fire event
				fireAfterRequestFlow(flowIndex, exception);
			}

			// In case of an exception, we don't want to execute potential subsequent request flows.
			if (exception != null) {
				break;
			}
		}
	}

	private void fireBeforeRequestFlow(final int flowIndex) {
		RequestFlowEvent event = new RequestFlowEvent(flowIndex);
		log.debug("fireBeforeRequestFlow: {}", event);
		for (RequestFlowEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.beforeRequestFlow(event);
		}
	}

	private void fireAfterRequestFlow(final int flowIndex, final Exception ex) {
		RequestFlowEvent event = new RequestFlowEvent(flowIndex, ex);
		log.debug("fireAfterRequestFlow: {}", event);
		for (RequestFlowEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.afterRequestFlow(event);
		}
	}

	private void fireBeforeRequest(final int flowIndex, final RequestTemplate template) {
		RequestFlowEvent event = new RequestFlowEvent(flowIndex, template);
		log.debug("fireBeforeRequestTemplate: {}", event);
		for (RequestFlowEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.beforeRequest(event);
		}
	}

	private void fireAfterRequest(final int flowIndex, final RequestTemplate template,
			final Exception ex, final ResponseInfo responseInfo) {
		RequestFlowEvent event = new RequestFlowEvent(flowIndex, template, ex, responseInfo);
		log.debug("fireAfterRequestTemplate: {}", event);
		for (RequestFlowEventListener listener : listeners) {
			log.debug("Executing listener: {}", listener);
			listener.afterRequest(event);
		}
	}
}
