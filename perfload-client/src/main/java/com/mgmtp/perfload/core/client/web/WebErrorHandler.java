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
package com.mgmtp.perfload.core.client.web;

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import com.mgmtp.perfload.core.client.runner.DefaultErrorHandler;
import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.client.web.request.InvalidRequestHandlerException;

/**
 * <p>
 * {@link ErrorHandler} subclass for Web tests. Additionally, instances of the following exceptions
 * trigger the abortion of a test:
 * </p>
 * <p>
 * <ul>
 * <li>{@link InvalidRequestHandlerException}</li>
 * </ul>
 * </p>
 * 
 * @author rnaegele
 */
@Singleton
@Immutable
@ThreadSafe
public class WebErrorHandler extends DefaultErrorHandler {

	public WebErrorHandler() {
		abortionTriggers.add(InvalidRequestHandlerException.class);
	}
}
