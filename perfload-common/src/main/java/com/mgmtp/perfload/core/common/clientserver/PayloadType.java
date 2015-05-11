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
package com.mgmtp.perfload.core.common.clientserver;

/**
 * Enum for the payload type.
 * 
 * @author rnaegele
 */
public enum PayloadType {
	/** Test is aborted by the console due to an error. */
	ABORT,

	/** An error occurred. */
	ERROR,

	/** Triggers the creation of a test process. Sent by the console to the daemons. */
	CREATE_TEST_PROC,

	/** Configuration is sent to the daemons that send the relevant parts on to the test processes. */
	CONFIG,

	/** Jars are sent to the daemons */
	JAR,

	/** The start of the test is triggered. */
	START,

	/** Status information is sent by the processes. */
	STATUS,

	/** Sent by the daemon to the console when a process has been started. **/
	TEST_PROC_STARTED,

	/** Sent by the daemon to the console when a process has terminated. **/
	TEST_PROC_TERMINATED,

	/** Sent by a test process to the daemon notifying it is ready for the test to start. Passed on to the console. **/
	TEST_PROC_READY,

	/** Sent by a test process to the daemon after it has successfully connected to it. Passed on to the console. **/
	TEST_PROC_CONNECTED,

	/**
	 * Sent by a test process to the daemon before actually disconnecting. Passed on to the console and pinged back to the test
	 * process so it know it can actually disconnect and shutdown.
	 **/
	TEST_PROC_DISCONNECTED,

	/**
	 * Signals a daemon that the console is going to disconnect.
	 */
	CONSOLE_DISCONNECTING,

	/**
	 * Signals a daemon that it should shut down.
	 */
	SHUTDOWN_DAEMON,

	/**
	 * Provides the number of connected clients.
	 */
	CLIENT_COUNT;
}
