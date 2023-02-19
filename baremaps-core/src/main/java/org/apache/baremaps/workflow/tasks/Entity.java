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

package org.apache.baremaps.workflow.tasks;

import java.util.HashMap;
import java.util.Map;
import org.apache.baremaps.feature.Feature;
import org.apache.baremaps.feature.FeatureType;
import org.apache.baremaps.feature.PropertyType;
import org.locationtech.jts.geom.Geometry;

public class Entity implements Feature {

  private final long id;

  private final Map<String, String> tags;

  private final Geometry geometry;

  public Entity(long id, Map<String, String> tags, Geometry geometry) {
    this.id = id;
    this.tags = tags;
    this.geometry = geometry;
  }


  @Override
  public FeatureType getType() {
    var map = new HashMap<String, PropertyType>();
    map.put("id", new PropertyType<>("id", Long.class));
    for (var entry : tags.entrySet()) {
      map.put(entry.getKey(), new PropertyType<>(entry.getKey(), String.class));
    }
    map.put("geometry", new PropertyType<>("geometry", Geometry.class));
    return new FeatureType("entity", map);
  }

  @Override
  public void setProperty(String name, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getProperty(String name) {
    if (name.equals("id")) {
      return id;
    } else if (name.equals("geometry")) {
      return geometry;
    } else {
      return tags.get(name);
    }
  }

  @Override
  public Map<String, Object> getProperties() {
    var map = new HashMap<String, Object>();
    map.put("id", id);
    map.putAll(tags);
    map.put("geometry", geometry);
    return map;
  }

  public long getId() {
    return id;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public Geometry getGeometry() {
    return geometry;
  }
}
