package com.mgmtp.perfload.test.utils;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.perfload.core.client.event.LtProcessEvent;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.runner.LtRunner;
import com.mgmtp.perfload.core.common.util.LtStatus;

/**
 * @author rnaegele
 * @since 4.7.1
 */
class TestExecutor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LtRunner runner;
	private final Set<LtProcessEventListener> listeners;

	@Inject
	TestExecutor(final LtRunner runner, final Set<LtProcessEventListener> listeners) {
		this.runner = runner;
		this.listeners = listeners;
	}

	void runDriver() {
		LtStatus status = LtStatus.ERROR;
		fireProcessStarted();
		try {
			runner.execute();
			status = LtStatus.SUCCESSFUL;
		} finally {
			fireProcessFinished(status);
		}
	}

	private void fireProcessStarted() {
		LtProcessEvent event = new LtProcessEvent(1, 1);
		logger.debug("fireProcessStarted: {}", event);

		for (LtProcessEventListener listener : listeners) {
			logger.debug("Executing listener: {}", listener);
			listener.processStarted(event);
		}
	}

	private void fireProcessFinished(final LtStatus result) {
		LtProcessEvent event = new LtProcessEvent(1, 1, result);
		logger.debug("fireProcessFinished: {}", event);

		for (LtProcessEventListener listener : listeners) {
			logger.debug("Executing listener: {}", listener);
			listener.processFinished(event);
		}
	}
}