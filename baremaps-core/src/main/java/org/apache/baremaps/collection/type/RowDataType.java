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
import org.apache.baremaps.collection.store.DataColumn;
import org.apache.baremaps.collection.store.DataRow;
import org.apache.baremaps.collection.store.DataRowImpl;
import org.apache.baremaps.collection.store.DataSchema;
import org.apache.baremaps.collection.type.geometry.*;
import org.locationtech.jts.geom.*;

public class RowDataType implements DataType<DataRow> {

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

  private final DataSchema dataSchema;

  public RowDataType(DataSchema dataSchema) {
    this.dataSchema = dataSchema;
  }

  @Override
  public int size(final DataRow dataRow) {
    var size = Integer.BYTES;
    var columns = dataSchema.columns();
    for (int i = 0; i < columns.size(); i++) {
      var columnType = columns.get(i).type();
      var dataType = types.get(columnType);
      var value = dataRow.get(i);
      size += dataType.size(value);
    }
    return size;
  }

  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  @Override
  public void write(final ByteBuffer buffer, final int position, final DataRow dataRow) {
    var p = position + Integer.BYTES;
    var columns = dataSchema.columns();
    for (int i = 0; i < columns.size(); i++) {
      var column = columns.get(i);
      var columnType = column.type();
      var dataType = types.get(columnType);
      var value = dataRow.get(i);
      dataType.write(buffer, p, value);
      p += dataType.size(buffer, p);
    }
    buffer.putInt(position, p - position);
  }

  @Override
  public DataRow read(final ByteBuffer buffer, final int position) {
    var p = position + Integer.BYTES;
    var columns = dataSchema.columns();
    var values = new ArrayList();
    for (DataColumn column : columns) {
      var columnType = column.type();
      var dataType = types.get(columnType);
      values.add(dataType.read(buffer, p));
      p += dataType.size(buffer, p);
    }
    return new DataRowImpl(dataSchema, values);
  }
}
