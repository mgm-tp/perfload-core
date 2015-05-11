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
package com.mgmtp.perfload.core.client.web.constants;

/**
 * @author rnaegele
 */
public class WebConstants {
	public static final String TRUST_STORE = "javax.net.ssl.trustStore";
	public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
	public static final String TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";

	public static final String KEY_STORE = "javax.net.ssl.keyStore";
	public static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
	public static final String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";

	public static final String EXECUTION_ID_HEADER = "X-perfLoad-Execution-Id";
	public static final String OPERATION_HEADER = "X-perfLoad-Operation";
	public static final String REQUEST_ID_HEADER = "X-perfLoad-Request-Id";
}
