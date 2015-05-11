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
package com.mgmtp.perfload.core.client.runner;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

import org.testng.annotations.Test;

import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;

/**
 * @author rnaegele
 */
public class DefaultErrorHandlerTest {

	@Test
	public void testUnhandledException() {
		ErrorHandler eh = new DefaultErrorHandler();
		eh.execute(new RuntimeException());
	}

	@Test()
	public void testHandledException() {
		ErrorHandler eh = new DefaultErrorHandler();
		try {
			eh.execute(new UnknownHostException());
			fail("Expected AbortionException was not thrown.");
		} catch (AbortionException ex) {
			assertTrue(ex.getCause() instanceof UnknownHostException);
		}
	}

	@Test(expectedExceptions = AbortionException.class)
	public void testAbortionException() {
		ErrorHandler eh = new DefaultErrorHandler();
		eh.execute(new AbortionException(LtStatus.ERROR));
	}

	@Test
	public void testInvocationTargetException() {
		ErrorHandler eh = new DefaultErrorHandler();
		try {
			eh.execute(new InvocationTargetException(new UnknownHostException("")));
			fail("Expected AbortionException was not thrown.");
		} catch (AbortionException ex) {
			assertTrue(ex.getCause() instanceof UnknownHostException);
		}
	}
}
