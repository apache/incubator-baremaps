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

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.baremaps.collection.type.*;
import org.locationtech.jts.geom.Geometry;

public class EntityDataType implements DataType<Entity> {

  private LongDataType idType = new LongDataType();

  private MapDataType<String, String> tagsType =
      new MapDataType<>(new StringDataType(), new StringDataType());

  private GeometryDataType geometryType = new GeometryDataType();

  @Override
  public int size(Entity value) {
    int size = 0;
    size += idType.size();
    size += tagsType.size(value.getTags());
    size += geometryType.size(value.getGeometry());
    return size;
  }

  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  @Override
  public void write(ByteBuffer buffer, int position, Entity value) {
    buffer.putInt(position, size(value));
    position += Integer.BYTES;
    idType.write(buffer, position, value.getId());
    position += idType.size();
    tagsType.write(buffer, position, value.getTags());
    position += tagsType.size(value.getTags());
    geometryType.write(buffer, position, value.getGeometry());
  }

  @Override
  public Entity read(ByteBuffer buffer, int position) {
    position += Integer.BYTES;
    long id = idType.read(buffer, position);
    position += idType.size(id);
    Map<String, String> tags = tagsType.read(buffer, position);
    position += tagsType.size(tags);
    Geometry geometry = geometryType.read(buffer, position);
    return new Entity(id, tags, geometry);
  }
}
