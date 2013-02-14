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
package com.mgmtp.perfload.core.client.runner;

import static com.google.common.collect.Sets.newHashSet;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Set;

import javax.inject.Singleton;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.common.util.AbortionException;
import com.mgmtp.perfload.core.common.util.LtStatus;

/**
 * <p>
 * Handles errors during test execution. By default, instances of the following exceptions trigger
 * the abortion of a test:
 * </p>
 * <p>
 * <ul>
 * <li>{@link Error}</li>
 * <li>{@link UnknownHostException}</li>
 * </ul>
 * </p>
 * <p>
 * Causion must be taken when overriding this class. In order to keep the class immutable and
 * thread-safe, changes to the internal set {@link #abortionTriggers} must only be made in a
 * subclass' constructor. Otherwise changes to this set might not be visible to other threads if
 * they read it before the changes are made.
 * </p>
 * 
 * @author rnaegele
 */
@Singleton
@ThreadSafe
@Immutable
public class DefaultErrorHandler implements ErrorHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected final Set<Class<? extends Throwable>> abortionTriggers = newHashSet();

	public DefaultErrorHandler() {
		abortionTriggers.add(Error.class);
		abortionTriggers.add(UnknownHostException.class);
	}

	/**
	 * Handles exceptions thrown during test execution. Implementors may decide whether an exception
	 * should lead to the abortion of the test. If the test is to be aborted, an
	 * {@link AbortionException} must be thrown.
	 * 
	 * @param th
	 *            the throwable
	 * @throws AbortionException
	 *             if the test is to be aborted
	 */
	@Override
	public final void execute(final Throwable th) throws AbortionException {
		if (th instanceof AbortionException) {
			log.warn("Test is being aborted...");
			throw (AbortionException) th;
		}

		if (th instanceof InvocationTargetException) {
			execute(th.getCause());
			return;
		}

		for (Class<? extends Throwable> throwable : abortionTriggers) {
			if (throwable.isInstance(th)) {
				log.warn("Test is being aborted...");
				throw new AbortionException(LtStatus.ERROR, th.getMessage(), th);
			}
		}

		log.error(th.getMessage(), th);
	}
}
