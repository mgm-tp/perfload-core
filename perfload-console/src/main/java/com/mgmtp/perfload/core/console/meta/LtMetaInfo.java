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
package com.mgmtp.perfload.core.console.meta;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;

/**
 * Pojo that holds meta information on a test run.
 * 
 * @author rnaegele
 */
public class LtMetaInfo {

	private final List<Daemon> daemonList = newArrayList();
	private final List<PlannedExecutions> plannedExecutionsList = newArrayList();
	private String testplanFileName;
	private final List<String> lpTargets = newArrayList();
	private final List<String> lpOperations = newArrayList();
	private String lpTestplanId;
	private long startTimestamp;
	private long finishTimestamp;

	/**
	 * @return the startTimestamp
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @param startTimestamp
	 *            the startTimestamp to set
	 */
	public void setStartTimestamp(final long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * @return the finishTimestamp
	 */
	public long getFinishTimestamp() {
		return finishTimestamp;
	}

	/**
	 * @param finishTimestamp
	 *            the finishTimestamp to set
	 */
	public void setFinishTimestamp(final long finishTimestamp) {
		this.finishTimestamp = finishTimestamp;
	}

	/**
	 * Adds daemon information.
	 * 
	 * @param id
	 *            the daemon's id
	 * @param host
	 *            the daemon's host
	 * @param port
	 *            the daemon's port
	 */
	public void addDaemon(final int id, final String host, final int port) {
		daemonList.add(new Daemon(id, host, port));
	}

	/**
	 * @return the testplanFileName
	 */
	public String getTestplanFileName() {
		return testplanFileName;
	}

	/**
	 * @param testplanFileName
	 *            the testplanFileName
	 */
	public void setTestplanFile(final String testplanFileName) {
		this.testplanFileName = testplanFileName;
	}

	/**
	 * Sets information on a load profile test.
	 * 
	 * @param lpTargets
	 *            a set of targets used in the load profile
	 * @param lpOperations
	 *            a set of operations used in the load profile
	 */
	public void setLoadProfileTestInfo(final Set<String> lpTargets, final Set<String> lpOperations) {
		this.lpTargets.addAll(lpTargets);
		this.lpOperations.addAll(lpOperations);
	}

	/**
	 * Adds planned executions by operation and target. Multiple calls of this method for the same
	 * operation/target pair sum up the executions.
	 * 
	 * @param operation
	 *            the operation
	 * @param target
	 *            the target
	 * @param executions
	 *            the number of executions for the specified operation and target
	 */
	public void addPlannedExecutions(final String operation, final String target, final int executions) {
		PlannedExecutions pe = new PlannedExecutions(operation, target, executions);
		int index = plannedExecutionsList.indexOf(pe);
		if (index >= 0) {
			// In case of static test we might already have an entry.
			PlannedExecutions oldPe = plannedExecutionsList.get(index);
			pe = new PlannedExecutions(operation, target, executions + oldPe.executions);
		}
		plannedExecutionsList.add(pe);
	}

	/**
	 * @return ther daemonList
	 */
	public List<Daemon> getDaemonList() {
		return daemonList;
	}

	/**
	 * @return the plannedExecutionsList
	 */
	public List<PlannedExecutions> getPlannedExecutionsList() {
		return plannedExecutionsList;
	}

	/**
	 * @return the lpOperations
	 */
	public List<String> getLpOperations() {
		return lpOperations;
	}

	/**
	 * @return the lpTargets
	 */
	public List<String> getLpTargets() {
		return lpTargets;
	}

	/**
	 * @return the lpTestplanId
	 */
	public String getLpTestplanId() {
		return lpTestplanId;
	}

	/**
	 * @param lpTestplanId
	 *            the lpTestplanId
	 */
	public void setLpTestplanId(final String lpTestplanId) {
		this.lpTestplanId = lpTestplanId;
	}

	static class Daemon implements Comparable<Daemon> {
		final int id;
		final String host;
		final int port;

		Daemon(final int id, final String host, final int port) {
			this.id = id;
			this.host = host;
			this.port = port;
		}

		@Override
		public int compareTo(final Daemon o) {
			int result = id - o.id;
			if (result == 0) {
				result = host.compareTo(o.host);
			}
			if (result == 0) {
				result = port - o.port;
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + host.hashCode();
			result = prime * result + id;
			result = prime * result + port;
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
			Daemon other = (Daemon) obj;
			if (!host.equals(other.host)) {
				return false;
			}
			if (id != other.id) {
				return false;
			}
			if (port != other.port) {
				return false;
			}
			return true;
		}
	}

	static class PlannedExecutions implements Comparable<PlannedExecutions> {
		final String operation;
		final String target;
		final int executions;

		PlannedExecutions(final String operation, final String target, final int executions) {
			this.operation = operation;
			this.target = target;
			this.executions = executions;
		}

		@Override
		public int compareTo(final PlannedExecutions o) {
			int result = operation.compareTo(o.operation);
			if (result == 0) {
				result = target.compareTo(o.target);
			}
			if (result == 0) {
				result = executions - o.executions;
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + executions;
			result = prime * result + operation.hashCode();
			result = prime * result + target.hashCode();
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
			PlannedExecutions other = (PlannedExecutions) obj;
			if (executions != other.executions) {
				return false;
			}
			if (!operation.equals(other.operation)) {
				return false;
			}
			if (!target.equals(other.target)) {
				return false;
			}
			return true;
		}
	}
}
