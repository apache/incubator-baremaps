/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.rpsl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a RPSL Object.
 */
public record RpslObject(List<RpslAttribute> attributes) {

  /**
   * Constructs a RPSL Object.
   *
   * @param attributes a list of RPSL attributes
   */
  public RpslObject {
    checkNotNull(attributes);
    checkArgument(!attributes.isEmpty());
  }

  /**
   * Returns the type of the RPSL object.
   *
   * @return the type of the RPSL object
   */
  public String type() {
    return attributes.get(0).name();
  }

  /**
   * Returns the id of the RPSL object.
   *
   * @return the id of the RPSL object
   */
  public String id() {
    return attributes.get(0).value();
  }

  /**
   * Returns the first attribute value matching the attribute name.
   *
   * @param name the attribute name
   * @return the attribute value
   */
  public Optional<String> first(String name) {
    return attributes.stream()
        .filter(attribute -> attribute.name().equals(name))
        .map(RpslAttribute::value)
        .findFirst();
  }

  /**
   * Returns all the attribute values matching the attribute name.
   *
   * @param name the attribute name
   * @return the attribute values
   */
  public List<String> all(String name) {
    return attributes.stream()
        .filter(attribute -> attribute.name().equals(name))
        .map(RpslAttribute::value)
        .toList();
  }

  /**
   * Return the attributes as a map.
   *
   * @return the attributes as a map
   */
  public Map<String, String> asMap() {
    var map = new HashMap<String, String>();
    for (RpslAttribute attribute : attributes()) {
      map.put(attribute.name(),
          (map.containsKey(attribute.name()) ? map.get(attribute.name()) + ", " : "")
              + attribute.value());
    }
    return map;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    var stringBuilder = new StringBuilder();
    for (RpslAttribute attribute : attributes()) {
      stringBuilder.append(attribute.name()).append(": ").append(attribute.value()).append("\n");
    }
    return stringBuilder.toString();
  }
}
