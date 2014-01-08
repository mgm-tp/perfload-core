/*
 * Copyright (c) 2002-2014 mgm technology partners GmbH
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
package com.mgmtp.perfload.core.client.web.response;

/**
 * Interface for a response validator.
 * 
 * @author rnaegele
 */
public interface ResponseValidator {

	/**
	 * Validates the response. Implementations may e. g. parse the response body for certain error
	 * indicators or check the HTTP status code.
	 * 
	 * @param responseInfo
	 *            the {@link ResponseInfo} object wrapping response information
	 * @throws InvalidResponseException
	 *             if the response is not valid
	 */
	void validate(final ResponseInfo responseInfo) throws InvalidResponseException;
}
