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
package com.mgmtp.perfload.core.clientserver.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.filterKeys;

import java.util.Collection;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mgmtp.perfload.core.clientserver.server.DefaultServer;

/**
 * Container for channels connected to a {@link DefaultServer}. The server maintains a
 * {@link ChannelContainer}, adding client channels to it when they connect and removing them when
 * they disconnect.
 * 
 * @author rnaegele
 */
public class ChannelContainer {

	private final BiMap<String, Channel> channels = HashBiMap.create(5);

	/**
	 * Adds the channel with the specified id. No two channels with the same id may be added.
	 * 
	 * @param id
	 *            the id
	 * @param channel
	 *            the channel
	 */
	public synchronized void addChannel(final String id, final Channel channel) {
		checkArgument(!channels.containsKey(id), "A channel with clientId " + id + " has already been added.");
		channels.put(id, channel);
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channel
	 *            the channel the channel to remove
	 */
	public synchronized void removeChannel(final Channel channel) {
		channels.inverse().remove(channel);
	}

	/**
	 * Gets the channel with the specified id.
	 * 
	 * @param id
	 *            the id
	 * @return the channel
	 */
	public synchronized Channel getChannel(final String id) {
		return channels.get(id);
	}

	/**
	 * Returns a collection of all channels in the container.
	 * 
	 * @return an immutable collection of channels which does not reflect changes to the container
	 *         so it may be safely iterated without synchronizing
	 */
	public synchronized Collection<Channel> getChannels() {
		return ImmutableMap.copyOf(channels).values();
	}

	/**
	 * Returns a collection of channels whose ids match the specified predicate.
	 * 
	 * @param idFilter
	 *            the predicate for filtering the channels
	 * @return an immutable collection of channels which does not reflect changes to the container
	 *         so it may be safely iterated without synchronizing
	 */
	public synchronized Collection<Channel> getChannels(final Predicate<String> idFilter) {
		return filterKeys(ImmutableMap.copyOf(channels), idFilter).values();
	}

	/**
	 * Returns a collection of channels whose ids match the specified predicate.
	 * 
	 * @param idFilter
	 *            the predicate for filtering the channels
	 * @return an immutable collection of channels which does not reflect changes to the container
	 *         so it may be safely iterated without synchronizing
	 */
	public synchronized Map<String, Channel> getChannelsMap(final Predicate<String> idFilter) {
		return filterKeys(ImmutableMap.copyOf(channels), idFilter);
	}

	/**
	 * Returns the single channel whose ids matches the specified predicate.
	 * 
	 * @param idFilter
	 *            the predicate for filtering the channels
	 * @return the channel
	 */
	public synchronized Channel getChannel(final Predicate<String> idFilter) {
		return getOnlyElement(filterKeys(channels, idFilter).values());
	}
}
