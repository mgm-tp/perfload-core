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
package com.mgmtp.perfload.core.client.config;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.mgmtp.perfload.core.client.driver.LtDriver;

/**
 * Provides a DSL for registering custom drivers.
 * 
 * @author rnaegele
 * @since 4.7.0
 */
public class DriverBindingBuilder {

	private final String key;
	private final MapBinder<String, LtDriver> driverMapBinder;
	private final MapBinder<String, DriverSelectionPredicate> predicateMapBinder;

	DriverBindingBuilder(final String key, final MapBinder<String, LtDriver> driverMapBinder,
			final MapBinder<String, DriverSelectionPredicate> predicateMapBinder) {
		this.key = key;
		this.driverMapBinder = driverMapBinder;
		this.predicateMapBinder = predicateMapBinder;

	}

	/**
	 * Returns a binding builder for adding a {@link LtDriver} implementation.
	 * 
	 * @param selectionPredicate
	 *            the predicate that must apply for the driver added to the returned binding builder
	 * @return the binding builder
	 */
	public LinkedBindingBuilder<LtDriver> forPredicate(final DriverSelectionPredicate selectionPredicate) {
		predicateMapBinder.addBinding(key).toInstance(selectionPredicate);
		return driverMapBinder.addBinding(key);
	}
}
