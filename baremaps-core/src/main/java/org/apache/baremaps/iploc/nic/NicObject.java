/*
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

package org.apache.baremaps.iploc.nic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Represents a NIC Object. */
public class NicObject {

  private final List<NicAttribute> attributes;

  /**
   * Constructs a NIC Object.
   *
   * @param attributes a list of NIC attributes
   */
  public NicObject(List<NicAttribute> attributes) {
    checkNotNull(attributes);
    checkArgument(!attributes.isEmpty());
    this.attributes = attributes;
  }

  /**
   * Returns the type of the NIC object.
   *
   * @return the type of the NIC object
   */
  public String type() {
    return attributes.get(0).name();
  }

  /**
   * Returns the id of the NIC object.
   *
   * @return the id of the NIC object
   */
  public String id() {
    return attributes.get(0).value();
  }

  /**
   * Returns the attributes of the NIC object.
   *
   * @return the attributes of the NIC object
   */
  public List<NicAttribute> attributes() {
    return Collections.unmodifiableList(attributes);
  }

  /**
   * Returns the first attribute value matching the attribute name.
   *
   * @param name the attribute name
   * @return the attribute value
   */
  public Optional<String> first(String name) {
    return attributes.stream().filter(attr -> attr.name().equals(name)).map(NicAttribute::value)
        .findFirst();
  }

  /**
   * Returns all the attribute values matching the attribute name.
   *
   * @param name the attribute name
   * @return the attribute values
   */
  public List<String> all(String name) {
    return attributes.stream().filter(attr -> attr.name().equals(name)).map(NicAttribute::value)
        .toList();
  }

  /**
   * Return the attributes as a map
   *
   * @return
   */
  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();
    for (NicAttribute attr : attributes()) {
      map.put(attr.name(),
          (map.containsKey(attr.name()) ? map.get(attr.name()) + ", " : "") + attr.value());
    }
    return map;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for (NicAttribute attr : attributes()) {
      str.append(attr.name()).append(": ").append(attr.value()).append("\n");
    }
    return str.toString();
  }
}
