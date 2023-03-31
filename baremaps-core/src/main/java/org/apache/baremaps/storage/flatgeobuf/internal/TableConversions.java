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

package org.apache.baremaps.storage.flatgeobuf.internal;


import com.google.flatbuffers.FlatBufferBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.baremaps.storage.*;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Crs;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.Header;

public class TableConversions {

  public static Schema asFeatureType(HeaderMeta headerMeta) {
    var name = headerMeta.name;
    var columns = headerMeta.columns.stream()
        .map(column -> new ColumnImpl(column.name, column.getBinding()))
        .map(Column.class::cast)
        .toList();
    return new SchemaImpl(name, columns);
  }

  public static Row asRow(HeaderMeta headerMeta, Schema dataType, Feature feature) {
    var values = new ArrayList();

    var geometryBuffer = feature.geometry();
    var geometry = GeometryConversions.deserialize(geometryBuffer, geometryBuffer.type());
    values.add(geometry);

    if (feature.propertiesLength() > 0) {
      var propertiesBuffer = feature.propertiesAsByteBuffer();
      while (propertiesBuffer.hasRemaining()) {
        var type = propertiesBuffer.getShort();
        var column = headerMeta.columns.get(type);
        var value = readValue(propertiesBuffer, column);
        values.add(value);
      }
    }

    return new RowImpl(dataType, values);
  }

  public static void writeHeaderMeta(HeaderMeta headerMeta, WritableByteChannel channel,
      FlatBufferBuilder builder) throws IOException {
    int[] columnsArray = headerMeta.columns.stream().mapToInt(c -> {
      int nameOffset = builder.createString(c.name);
      int type = c.type;
      return org.wololo.flatgeobuf.generated.Column.createColumn(builder, nameOffset, type, 0, 0,
          c.width, c.precision, c.scale, c.nullable, c.unique,
          c.primary_key, 0);
    }).toArray();
    int columnsOffset = Header.createColumnsVector(builder, columnsArray);

    int nameOffset = 0;
    if (headerMeta.name != null) {
      nameOffset = builder.createString(headerMeta.name);
    }
    int crsOffset = 0;
    if (headerMeta.srid != 0) {
      Crs.startCrs(builder);
      Crs.addCode(builder, headerMeta.srid);
      crsOffset = Crs.endCrs(builder);
    }
    int envelopeOffset = 0;
    if (headerMeta.envelope != null) {
      envelopeOffset = Header.createEnvelopeVector(builder,
          new double[] {headerMeta.envelope.getMinX(), headerMeta.envelope.getMinY(),
              headerMeta.envelope.getMaxX(), headerMeta.envelope.getMaxY()});
    }
    Header.startHeader(builder);
    Header.addGeometryType(builder, headerMeta.geometryType);
    Header.addIndexNodeSize(builder, headerMeta.indexNodeSize);
    Header.addColumns(builder, columnsOffset);
    Header.addEnvelope(builder, envelopeOffset);
    Header.addName(builder, nameOffset);
    Header.addCrs(builder, crsOffset);
    Header.addFeaturesCount(builder, headerMeta.featuresCount);
    int offset = Header.endHeader(builder);

    builder.finishSizePrefixed(offset);

    ByteBuffer dataBuffer = builder.dataBuffer();
    while (dataBuffer.hasRemaining())
      channel.write(dataBuffer);
  }

  public static Object readValue(ByteBuffer propertiesBuffer, ColumnMeta column) {
    return switch (column.type) {
      case ColumnType.Byte -> propertiesBuffer.get();
      case ColumnType.Bool -> propertiesBuffer.get() == 1;
      case ColumnType.Short -> propertiesBuffer.getShort();
      case ColumnType.Int -> propertiesBuffer.getInt();
      case ColumnType.Long -> propertiesBuffer.getLong();
      case ColumnType.Float -> propertiesBuffer.getFloat();
      case ColumnType.Double -> propertiesBuffer.getDouble();
      case ColumnType.String -> readString(propertiesBuffer);
      case ColumnType.Json -> readJson(propertiesBuffer);
      case ColumnType.DateTime -> readDateTime(propertiesBuffer);
      case ColumnType.Binary -> readBinary(propertiesBuffer);
      default -> null;
    };
  }

  public static void writeValue(ByteBuffer propertiesBuffer, ColumnMeta column, Object value) {
    switch (column.type) {
      case ColumnType.Byte -> propertiesBuffer.put((byte) value);
      case ColumnType.Bool -> propertiesBuffer.put((byte) ((boolean) value ? 1 : 0));
      case ColumnType.Short -> propertiesBuffer.putShort((short) value);
      case ColumnType.Int -> propertiesBuffer.putInt((int) value);
      case ColumnType.Long -> propertiesBuffer.putLong((long) value);
      case ColumnType.Float -> propertiesBuffer.putFloat((float) value);
      case ColumnType.Double -> propertiesBuffer.putDouble((double) value);
      case ColumnType.String -> writeString(propertiesBuffer, value);
      case ColumnType.Json -> writeJson(propertiesBuffer, value);
      case ColumnType.DateTime -> writeDateTime(propertiesBuffer, value);
      case ColumnType.Binary -> writeBinary(propertiesBuffer, value);
      default -> {
      }
    };
  }

  public static void writeString(ByteBuffer propertiesBuffer, Object value) {
    var bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
    propertiesBuffer.putInt(bytes.length);
    propertiesBuffer.put(bytes);
  }

  public static void writeJson(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static void writeDateTime(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static void writeBinary(ByteBuffer propertiesBuffer, Object value) {
    throw new UnsupportedOperationException();
  }

  public static Object readString(ByteBuffer buffer) {
    var length = buffer.getInt();
    var bytes = new byte[length];
    buffer.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static Object readJson(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  public static Object readDateTime(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  public static Object readBinary(ByteBuffer buffer) {
    throw new UnsupportedOperationException();
  }

  public static final Map<Class, Integer> types = Map.of(
      Byte.class, ColumnType.Byte,
      Boolean.class, ColumnType.Bool,
      Short.class, ColumnType.Short,
      Integer.class, ColumnType.Int,
      Long.class, ColumnType.Long,
      Float.class, ColumnType.Float,
      Double.class, ColumnType.Double,
      String.class, ColumnType.String);

  public static List<ColumnMeta> asColumns(List<Column> columns) {
    return columns.stream()
        .map(TableConversions::asColumn)
        .collect(Collectors.toList());
  }

  public static ColumnMeta asColumn(Column column) {
    var type = types.get(column.type());
    if (type == null) {
      throw new IllegalArgumentException("Unsupported type " + type);
    }
    var columnMeta = new ColumnMeta();
    columnMeta.name = column.name();
    columnMeta.type = type.byteValue();
    return columnMeta;
  }
}
