package com.mgmtp.perfload.core.web.event;

import com.mgmtp.perfload.core.client.event.LtProcessEvent;
import com.mgmtp.perfload.core.client.event.LtProcessEventListener;
import com.mgmtp.perfload.core.client.event.LtRunnerEvent;
import com.mgmtp.perfload.core.client.event.LtRunnerEventListener;

/**
 * Adapter class woth no-op implementations for perfLoad's event listeners.
 * 
 * @author rnaegele
 */
public class LtListenerAdapter implements LtProcessEventListener, LtRunnerEventListener, RequestFlowEventListener {

	// LtProcessEventListener

	@Override
	public void processStarted(final LtProcessEvent event) {
		// no-op
	}

	@Override
	public void processFinished(final LtProcessEvent event) {
		// no-op
	}

	// LtRunnerEventListener

	@Override
	public void runStarted(final LtRunnerEvent event) {
		// no-op
	}

	@Override
	public void runFinished(final LtRunnerEvent event) {
		// no-op
	}

	// RequestFlowEventListener

	@Override
	public void beforeRequestFlow(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void afterRequestFlow(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void beforeRequest(final RequestFlowEvent event) {
		// no-op
	}

	@Override
	public void afterRequest(final RequestFlowEvent event) {
		// no-op
	}
}
