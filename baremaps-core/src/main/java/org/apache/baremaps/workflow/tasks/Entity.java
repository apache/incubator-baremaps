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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.storage.*;
import org.locationtech.jts.geom.Geometry;

public class Entity implements Row {

  private long id;

  private Map<String, String> tags;

  private Geometry geometry;

  public Entity(long id, Map<String, String> tags, Geometry geometry) {
    this.id = id;
    this.tags = tags;
    this.geometry = geometry;
  }


  @Override
  public Schema schema() {
    var columns = new ArrayList<Column>();
    columns.add(new ColumnImpl("id", Long.class));
    columns.add(new ColumnImpl("geometry", Geometry.class));
    for (var entry : tags.entrySet()) {
      columns.add(new ColumnImpl(entry.getKey(), String.class));
    }
    return new SchemaImpl("entity", columns);
  }

  @Override
  public void set(String column, Object value) {
    tags.put(column, value.toString());
  }

  @Override
  public Object get(String column) {
    if (column.equals("id")) {
      return id;
    } else if (column.equals("geometry")) {
      return geometry;
    } else {
      return tags.get(column);
    }
  }

  @Override
  public List<Object> values() {
    var map = new ArrayList();
    map.add(id);
    map.add(geometry);
    map.addAll(tags.values());
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
