package io.gazetteer.postgis.util;

import io.gazetteer.postgis.metadata.ObjectIdentifier;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.copy.PGCopyOutputStream;

public class PGCopyWriter implements AutoCloseable {

  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final DataOutputStream data;

  public PGCopyWriter(PGCopyOutputStream data) {
    this.data = new DataOutputStream(new BufferedOutputStream(data));
  }

  public void writeHeader() throws IOException {
    // 11 bytes required header
    data.writeBytes("PGCOPY\n\377\r\n\0");
    // 32 bit integer indicating no OID
    data.writeInt(0);
    // 32 bit header extension area length
    data.writeInt(0);
  }

  public void startRow(int columns) throws IOException {
    data.writeShort(columns);
  }

  public void writeNull() throws IOException {
    data.writeInt(-1);
  }

  public void writeString(String value) throws IOException {
    nullableWriter(PGCopyWriter::stringWriter).write(data, value);
  }

  public void writeStringArray(String[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Text, PGCopyWriter::stringWriter)).write(data, value);
  }

  public void writeBoolean(Boolean value) throws IOException {
    nullableWriter(PGCopyWriter::booleanWriter).write(data, value);
  }

  public void writeBooleanArray(Boolean[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Boolean, PGCopyWriter::booleanWriter)).write(data, value);
  }

  public void writeByte(Byte value) throws IOException {
    nullableWriter(PGCopyWriter::byteWriter).write(data, value);
  }

  public void writeByteArray(byte[] value) throws IOException {
    nullableWriter(PGCopyWriter::byteArrayWriter).write(data, value);
  }

  public void writeShort(Short value) throws IOException {
    nullableWriter(PGCopyWriter::shortWriter).write(data, value);
  }

  public void writeShortArray(Short[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Int4, PGCopyWriter::shortWriter)).write(data, value);
  }

  public void writeInteger(Integer value) throws IOException {
    nullableWriter(PGCopyWriter::integerWriter).write(data, value);
  }

  public void writeIntegerArray(Integer[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Int4, PGCopyWriter::integerWriter)).write(data, value);
  }

  public void writeLong(Long value) throws IOException {
    nullableWriter(PGCopyWriter::longWriter).write(data, value);
  }

  public void writeLongArray(Long[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Int8, PGCopyWriter::longWriter)).write(data, value);
  }

  public void writeFloat(Float value) throws IOException {
    nullableWriter(PGCopyWriter::floatWriter).write(data, value);
  }

  public void writeFloatArray(Float[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Int8, PGCopyWriter::floatWriter)).write(data, value);
  }

  public void writeDouble(Double value) throws IOException {
    nullableWriter(PGCopyWriter::doubleWriter).write(data, value);
  }

  public void writeDoubleArray(Double[] value) throws IOException {
    nullableWriter(collectionWriter(ObjectIdentifier.Int8, PGCopyWriter::doubleWriter)).write(data, value);
  }

  public void writeHstore(Map<String, String> value) throws IOException {
    nullableWriter(PGCopyWriter::hstoreWriter).write(data, value);
  }

  public void writeGeometry(Geometry value) throws IOException {
    nullableWriter(PGCopyWriter::byteArrayWriter).write(data, new WKBWriter().write(value));
  }

  public void writeObject(Object value) throws IOException {
    if (value instanceof String) {
      writeString((String) value);
    } else if (value instanceof Boolean) {
      writeBoolean((Boolean) value);
    } else if (value instanceof Byte) {
      writeByte((Byte) value);
    } else if (value instanceof Short) {
      writeShort((Short) value);
    } else if (value instanceof Integer) {
      writeInteger((Integer) value);
    } else if (value instanceof Long) {
      writeLong((Long) value);
    } else if (value instanceof Float) {
      writeFloat((Float) value);
    } else if (value instanceof Double) {
      writeDouble((Double) value);
    } else if (value instanceof String[]) {
      writeStringArray((String[]) value);
    } else if (value instanceof Boolean[]) {
      writeBooleanArray((Boolean[]) value);
    } else if (value instanceof byte[]) {
      writeByteArray((byte[]) value);
    } else if (value instanceof Short[]) {
      writeShortArray((Short[]) value);
    } else if (value instanceof Integer[]) {
      writeIntegerArray((Integer[]) value);
    } else if (value instanceof Long[]) {
      writeLongArray((Long[]) value);
    } else if (value instanceof Float[]) {
      writeFloatArray((Float[]) value);
    } else if (value instanceof Double[]) {
      writeDoubleArray((Double[]) value);
    } else if (value instanceof Map) {
      writeHstore((Map<String, String>) value);
    } else if (value instanceof Geometry) {
      writeGeometry((Geometry) value);
    } else {
      throw new IOException("Unsupported type");
    }
  }

  @Override
  public void close() throws Exception {
    data.writeShort(-1);
    data.flush();
    data.close();
  }

  private static <T> ValueWriter<T> nullableWriter(ValueWriter<T> writer) {
    return (data, value) -> {
      if (value == null) {
        data.writeInt(-1);
        return;
      }
      writer.write(data, value);
    };
  }

  private static void booleanWriter(DataOutputStream data, Boolean value) throws IOException {
    data.writeInt(1);
    if (value) {
      data.writeByte(1);
    } else {
      data.writeByte(0);
    }
  }

  private static void byteWriter(DataOutputStream data, Byte value) throws IOException {
    data.writeInt(1);
    data.writeShort(value.byteValue());
  }

  private static void byteArrayWriter(DataOutputStream data, byte[] value) throws IOException {
    data.writeInt(value.length);
    data.write(value, 0, value.length);
  }

  private static void shortWriter(DataOutputStream data, Short value) throws IOException {
    data.writeInt(2);
    data.writeShort(value.shortValue());
  }

  private static void integerWriter(DataOutputStream data, Integer value) throws IOException {
    data.writeInt(4);
    data.writeInt(value.intValue());
  }

  private static void floatWriter(DataOutputStream data, Float value) throws IOException {
    data.writeInt(4);
    data.writeFloat(value.floatValue());
  }

  private static void doubleWriter(DataOutputStream data, Double value) throws IOException {
    data.writeInt(8);
    data.writeDouble(value.doubleValue());
  }

  private static void longWriter(DataOutputStream data, Long value) throws IOException {
    data.writeInt(8);
    data.writeLong(value.longValue());
  }

  private static void stringWriter(DataOutputStream data, String value) throws IOException {
    byte[] bytes = value.getBytes(UTF8);
    data.writeInt(bytes.length);
    data.write(bytes);
  }

  private <T> ValueWriter<T[]> collectionWriter(int oid, ValueWriter<T> writer) {
    return (data, values) -> {
      // Write into a temporary byte array
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

      // Use 1 for one-dimensional arrays
      dataOutputStream.writeInt(1);

      // The collection can contain null values
      dataOutputStream.writeInt(1);

      // Write the values using the OID
      dataOutputStream.writeInt(oid);

      // Write the number of elements
      dataOutputStream.writeInt(values.length);

      // Ignore Lower Bound. Use PG Default for now
      dataOutputStream.writeInt(1);

      // Iterate over the collection and write each values
      for (T value : values) {
        writer.write(data, value);
      }

      // Write the entire array to the COPY data:
      data.writeInt(byteArrayOutputStream.size());
      data.write(byteArrayOutputStream.toByteArray());
    };
  }

  private static void hstoreWriter(DataOutputStream data, Map<String, String> value) throws IOException {
    // Write into a temporary byte array
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    // Write the number of values to write
    dataOutputStream.writeInt(value.size());

    // Iterate over the map and write each key value pairs
    for (Map.Entry<String, String> entry : value.entrySet()) {
      stringWriter(dataOutputStream, entry.getKey());
      stringWriter(dataOutputStream, entry.getValue());
    }

    // Write the entire array to the COPY data
    data.writeInt(byteArrayOutputStream.size());
    data.write(byteArrayOutputStream.toByteArray());
  }

  @FunctionalInterface
  private interface ValueWriter<T> {

    void write(DataOutputStream data, T value) throws IOException;
  }

}
