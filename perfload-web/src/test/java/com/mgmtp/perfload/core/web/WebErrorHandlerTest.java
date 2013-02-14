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
package com.mgmtp.perfload.core.web;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.client.runner.ErrorHandler;
import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.web.request.InvalidRequestHandlerException;

/**
 * @author rnaegele
 */
public class WebErrorHandlerTest {

	@Test()
	public void testHandledException() {
		ErrorHandler eh = new WebErrorHandler();
		try {
			eh.execute(new InvalidRequestHandlerException("foo"));
			fail("Expected AbortionException was not thrown.");
		} catch (AbortionException ex) {
			assertTrue(ex.getCause() instanceof InvalidRequestHandlerException);
		}
	}
}
