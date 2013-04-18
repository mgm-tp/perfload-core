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
package com.mgmtp.perfload.core.client.web.event;

import com.mgmtp.perfload.core.client.web.flow.DefaultRequestFlowHandler;
import com.mgmtp.perfload.core.client.web.flow.RequestFlow;
import com.mgmtp.perfload.core.client.web.template.RequestTemplate;

/**
 * Event listener that can be registered with {@link DefaultRequestFlowHandler}.
 * 
 * @author rnaegele
 */
public interface RequestFlowEventListener {

	/**
	 * Executed before a {@link RequestFlow} is executed.
	 * 
	 * @param event
	 *            the event
	 */
	void beforeRequestFlow(RequestFlowEvent event);

	/**
	 * Executed after a {@link RequestFlow} was executed.
	 * 
	 * @param event
	 *            the event
	 */
	void afterRequestFlow(RequestFlowEvent event);

	/**
	 * Executed before a {@link RequestTemplate} is executed.
	 * 
	 * @param event
	 *            the event
	 */
	void beforeRequest(RequestFlowEvent event);

	/**
	 * Executed after a {@link RequestTemplate} was executed.
	 * 
	 * @param event
	 *            the event
	 */
	void afterRequest(RequestFlowEvent event);

}
