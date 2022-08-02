/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.model;

import com.baremaps.osm.function.EntityConsumer;
import com.baremaps.osm.function.EntityFunction;

/**
 * Represents an entity in an OpenStreetMap dataset. Entities are a basis to model all the objects
 * in OpenStreetMap.
 */
public interface Entity {

  /** Visits the entity with the provided entity consumer. */
  void visit(EntityConsumer consumer) throws Exception;

  /** Visits the entity with the provided entity function. */
  <T> T visit(EntityFunction<T> function) throws Exception;
}
