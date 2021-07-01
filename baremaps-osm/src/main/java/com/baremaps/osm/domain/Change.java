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

package com.baremaps.osm.domain;

import com.baremaps.osm.handler.ChangeConsumer;
import com.baremaps.osm.handler.ChangeFunction;
import com.baremaps.osm.handler.EntityConsumer;
import com.baremaps.osm.handler.EntityFunction;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a change in an OpenStreetMap dataset.
 */
public final class Change {

  @Override
  public String toString() {
    return new StringJoiner(", ", Change.class.getSimpleName() + "[", "]")
        .add("type=" + type)
        .add("elements=" + elements)
        .toString();
  }

  public enum ChangeType {
    delete,
    create,
    modify
  }

  private final ChangeType type;

  private final List<Element> elements;

  public Change(ChangeType type, List<Element> elements) {
    this.type = type;
    this.elements = elements;
  }

  public ChangeType getType() {
    return type;
  }

  public List<Element> getElements() {
    return elements;
  }

  /**
   * Visits the entity with the provided entity consumer.
   */
  public void visit(ChangeConsumer consumer) throws Exception {
    consumer.match(this);
  }

  /**
   * Visits the entity with the provided entity function.
   */
  public <T> T visit(ChangeFunction<T> function) throws Exception {
    return function.match(this);
  }

}
