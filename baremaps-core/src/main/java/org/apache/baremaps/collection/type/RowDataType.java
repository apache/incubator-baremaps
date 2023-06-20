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

package org.apache.baremaps.collection.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.baremaps.collection.store.Row;
import org.apache.baremaps.collection.store.RowImpl;
import org.apache.baremaps.collection.store.Schema;
import org.apache.baremaps.collection.type.geometry.*;
import org.locationtech.jts.geom.*;

public class RowDataType implements DataType<Row> {

  private static final Map<Class, DataType> types;

  static {
    types = new HashMap<>();
    types.put(Byte.class, new ByteDataType());
    types.put(Boolean.class, new BooleanDataType());
    types.put(Short.class, new ShortDataType());
    types.put(Integer.class, new IntegerDataType());
    types.put(Long.class, new LongDataType());
    types.put(Float.class, new FloatDataType());
    types.put(Double.class, new DoubleDataType());
    types.put(String.class, new StringDataType());
    types.put(Geometry.class, new GeometryDataType());
    types.put(Point.class, new PointDataType());
    types.put(LineString.class, new LineStringDataType());
    types.put(Polygon.class, new PolygonDataType());
    types.put(MultiPoint.class, new MultiPointDataType());
    types.put(MultiLineString.class, new MultiLineStringDataType());
    types.put(MultiPolygon.class, new MultiPolygonDataType());
    types.put(GeometryCollection.class, new GeometryCollectionDataType());
    types.put(Coordinate.class, new CoordinateDataType());
  }

  private final Schema schema;

  public RowDataType(Schema schema) {
    this.schema = schema;
  }

  @Override
  public int size(Row row) {
    var size = Integer.BYTES;
    var columns = schema.columns();
    for (int i = 0; i < columns.size(); i++) {
      var columnType = columns.get(i).type();
      var dataType = types.get(columnType);
      var value = row.get(i);
      size += dataType.size(value);
    }
    return size;
  }

  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  @Override
  public void write(final ByteBuffer buffer, final int position, final Row row) {
    var p = position + Integer.BYTES;
    var columns = schema.columns();
    for (int i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      var columnType = column.type();
      var dataType = types.get(columnType);
      var value = row.get(i);
      dataType.write(buffer, p, value);
      p += dataType.size(buffer, p);
    }
    buffer.putInt(position, p - position);
  }

  @Override
  public Row read(final ByteBuffer buffer, final int position) {
    var p = position + Integer.BYTES;
    var columns = schema.columns();
    var values = new ArrayList();
    for (int i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      var columnType = column.type();
      var dataType = types.get(columnType);
      values.add(dataType.read(buffer, p));
      p += dataType.size(buffer, p);
    }
    return new RowImpl(schema, values);
  }
}
