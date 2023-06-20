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

package org.apache.baremaps.storage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import org.apache.baremaps.collection.type.*;

public class RowDataType implements DataType<Row> {

  private static final Map<Class, DataType> types = Map.of(
      Byte.class, new ByteDataType(),
      Boolean.class, new BooleanDataType(),
      Short.class, new ShortDataType(),
      Integer.class, new IntegerDataType(),
      Long.class, new LongDataType(),
      Float.class, new FloatDataType(),
      Double.class, new DoubleDataType(),
      String.class, new StringDataType());

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
      dataType.write(buffer, position, value);
      p += dataType.size(buffer, position);
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
