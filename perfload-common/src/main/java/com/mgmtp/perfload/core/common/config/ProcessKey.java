package com.mgmtp.perfload.core.common.config;

import java.io.Serializable;

/**
 * @author rnaegele
 */
public class ProcessKey implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int daemonId;
	private final int processId;

	public ProcessKey(final int daemonId, final int processId) {
		this.daemonId = daemonId;
		this.processId = processId;
	}

	/**
	 * @return the daemonId
	 */
	public int getDaemonId() {
		return daemonId;
	}

	/**
	 * @return the processId
	 */
	public int getProcessId() {
		return processId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + daemonId;
		result = prime * result + processId;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessKey other = (ProcessKey) obj;
		if (daemonId != other.daemonId) {
			return false;
		}
		if (processId != other.processId) {
			return false;
		}
		return true;
	}
}
