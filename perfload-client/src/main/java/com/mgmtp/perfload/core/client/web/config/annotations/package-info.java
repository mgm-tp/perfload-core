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
/**
 * <p>
 * This package contains annotations related to JSR 330 or Guice.
 * Most of these annotations are binding annotations that further qualify
 * certain bindings. This is necessary if there are multiple bindings of the
 * same type. Using such annotations (which themselves must be annotated
 * with {@link javax.inject.Qualifier}, binding can be uniquely identified.
 * </p>
 * <p>
 * Parameters or fields that are subject to JSR 330 injection must be annotated with
 * the same qualifying annotation they are bound with, so Guice is able to inject the
 * correct objects.
 * </p>
 */
package com.mgmtp.perfload.core.client.web.config.annotations;