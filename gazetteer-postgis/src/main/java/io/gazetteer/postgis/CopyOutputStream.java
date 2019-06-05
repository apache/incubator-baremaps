package io.gazetteer.postgis;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

public class CopyOutputStream {

  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final DataOutputStream data;

  public CopyOutputStream(DataOutputStream data) {
    this.data = data;
  }

  public void writeString(String value) throws IOException {
    nullableWriter(CopyOutputStream::stringWriter).write(data, value);
  }

  public void writeBoolean(Boolean value) throws IOException {
    nullableWriter(CopyOutputStream::booleanWriter).write(data, value);
  }

  public void writeByte(Byte value) throws IOException {
    nullableWriter(CopyOutputStream::byteWriter).write(data, value);
  }

  public void writeByteArray(byte[] value) throws IOException {
    nullableWriter(CopyOutputStream::byteArrayWriter).write(data, value);
  }

  public void writeShort(Short value) throws IOException {
    nullableWriter(CopyOutputStream::shortWriter).write(data, value);
  }

  public void writeInteger(Integer value) throws IOException {
    nullableWriter(CopyOutputStream::integerWriter).write(data, value);
  }

  public void writeLong(Long value) throws IOException {
    nullableWriter(CopyOutputStream::longWriter).write(data, value);
  }

  public void writeFloat(Float value) throws IOException {
    nullableWriter(CopyOutputStream::floatWriter).write(data, value);
  }

  public void writeDouble(Double value) throws IOException {
    nullableWriter(CopyOutputStream::doubleWriter).write(data, value);
  }

  public void writeIntegerCollection(Collection<Integer> value) throws IOException {
    nullableWriter(collectionWriter(1, CopyOutputStream::integerWriter)).write(data, value);
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

  private <T> ValueWriter<Collection<T>> collectionWriter(int oid, ValueWriter<T> writer) {
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
      dataOutputStream.writeInt(values.size());

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
