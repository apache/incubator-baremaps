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

package org.apache.baremaps.database.copy;



import de.bytefish.pgbulkinsert.pgsql.handlers.*;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.Oid;

/** A helper for writing in a {@code PGCopyOutputStream}. */
public class CopyWriter implements AutoCloseable {

  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private static final byte IPV4 = 2;
  private static final byte IPV4_MASK = 32;
  private static final byte IPV4_IS_CIDR = 0;

  private static final byte IPV6 = 3;
  private static final int IPV6_MASK = 128;
  private static final byte IPV6_IS_CIDR = 0;

  private static final byte JSONB_VERSION = 1;

  private final DataOutputStream data;

  /**
   * Creates a new writer with the specified {@code PGCopyOutputStream}.
   *
   * <p>
   * This code has been adapted from
   * <a href="https://github.com/PgBulkInsert/PgBulkInsert">PgBulkInsert</a> licensed under the MIT
   * license.
   *
   * <p>
   * Copyright (c) The PgBulkInsert Team.
   *
   * @param data
   */
  public CopyWriter(PGCopyOutputStream data) {
    this.data = new DataOutputStream(new BufferedOutputStream(data, 65536));
  }

  /**
   * Writes the header of the query.
   *
   * @throws IOException
   */
  public void writeHeader() throws IOException {
    // 11 bytes required header
    data.writeBytes("PGCOPY\n\377\r\n\0");
    // 32 bit integer indicating no OID
    data.writeInt(0);
    // 32 bit header extension area length
    data.writeInt(0);
  }

  /**
   * Writes a null value.
   *
   * @throws IOException
   */
  public void writeNull() throws IOException {
    data.writeInt(-1);
  }

  /**
   * Writes a null value.
   *
   * @throws IOException
   */
  public <T> void write(BaseValueHandler<T> handler, T value) throws IOException {
    handler.handle(data, value);
  }

  /**
   * Writes the number of columns affected by the query.
   *
   * @param columns
   * @throws IOException
   */
  public void startRow(int columns) throws IOException {
    data.writeShort(columns);
  }

  /**
   * Writes a string value.
   *
   * @param value
   * @throws IOException
   */
  public void write(String value) throws IOException {
    new StringValueHandler().handle(data, value);
  }

  /**
   * Writes a list of string values.
   *
   * @param value
   * @throws IOException
   */
  public void write(List<String> value) throws IOException {
    new CollectionValueHandler<>(Oid.TEXT, new StringValueHandler()).handle(data, value);
  }

  /**
   * Writes a boolean value.
   *
   * @param value
   * @throws IOException
   */
  public void writeBoolean(Boolean value) throws IOException {
    new BooleanValueHandler().handle(data, value);
  }

  /**
   * Writes a list of boolean values.
   *
   * @param value
   * @throws IOException
   */
  public void writeBooleanList(List<Boolean> value) throws IOException {
    new CollectionValueHandler<>(Oid.BOOL, new BooleanValueHandler()).handle(data, value);
  }

  /**
   * Writes a byte value.
   *
   * @param value
   * @throws IOException
   */
  public void writeByte(Byte value) throws IOException {
    new ByteValueHandler<>().handle(data, value);
  }

  /**
   * Writes a byte array value.
   *
   * @param value
   * @throws IOException
   */
  public void writeByteArray(byte[] value) throws IOException {
    new ByteArrayValueHandler().handle(data, value);
  }

  /**
   * Writes a short value.
   *
   * @param value
   * @throws IOException
   */
  public void writeShort(Short value) throws IOException {
    new ShortValueHandler<>().handle(data, value);
  }

  /**
   * Writes a list of short values.
   *
   * @param value
   * @throws IOException
   */
  public void writeShortList(List<Short> value) throws IOException {
    new CollectionValueHandler<>(Oid.INT2, new ShortValueHandler<Short>()).handle(data, value);
  }

  /**
   * Writes an integer value.
   *
   * @param value
   * @throws IOException
   */
  public void writeInteger(Integer value) throws IOException {
    new IntegerValueHandler<>().handle(data, value);
  }

  /**
   * Writes a list of integer values.
   *
   * @param value
   * @throws IOException
   */
  public void writeIntegerList(List<Integer> value) throws IOException {
    new CollectionValueHandler<>(Oid.INT4, new IntegerValueHandler<Integer>()).handle(data, value);
  }

  /**
   * Writes a long value.
   *
   * @param value
   * @throws IOException
   */
  public void writeLong(Long value) throws IOException {
    new LongValueHandler<>().handle(data, value);
  }

  /**
   * Writes a list of long values.
   *
   * @param value
   * @throws IOException
   */
  public void writeLongList(List<Long> value) throws IOException {
    new CollectionValueHandler<>(Oid.INT8, new LongValueHandler<Long>()).handle(data, value);
  }

  /**
   * Writes a float value.
   *
   * @param value
   * @throws IOException
   */
  public void writeFloat(Float value) throws IOException {
    new FloatValueHandler<>().handle(data, value);
  }

  /**
   * Writes a list of float values.
   *
   * @param value
   * @throws IOException
   */
  public void writeFloatList(List<Float> value) throws IOException {
    new CollectionValueHandler<>(Oid.FLOAT4, new FloatValueHandler<Float>()).handle(data, value);
  }

  /**
   * Writes a double value.
   *
   * @param value
   * @throws IOException
   */
  public void writeDouble(Double value) throws IOException {
    new DoubleValueHandler<>().handle(data, value);
  }

  /**
   * Writes a list of double values.
   *
   * @param value
   * @throws IOException
   */
  public void writeDoubleArray(List<Double> value) throws IOException {
    new CollectionValueHandler<>(Oid.FLOAT8, new DoubleValueHandler<Double>()).handle(data, value);
  }

  /**
   * Writes a date value.
   *
   * @param value
   * @throws IOException
   */
  public void writeLocalDate(LocalDate value) throws IOException {
    new LocalDateValueHandler().handle(data, value);
  }

  /**
   * Writes a list of date values.
   *
   * @param value
   * @throws IOException
   */
  public void writeLocalDateTime(LocalDateTime value) throws IOException {
    new LocalDateTimeValueHandler().handle(data, value);
  }

  /**
   * Writes an inet adress value.
   *
   * @param value
   * @throws IOException
   */
  public void writeInet4Adress(Inet4Address value) throws IOException {
    new Inet4AddressValueHandler().handle(data, value);
  }

  /**
   * Writes a list of inet adress values.
   *
   * @param value
   * @throws IOException
   */
  public void writeInet6Adress(Inet6Address value) throws IOException {
    new Inet6AddressValueHandler().handle(data, value);
  }

  /**
   * Writes a map value.
   *
   * @param value
   * @throws IOException
   */
  public void writeHstore(Map<String, String> value) throws IOException {
    new HstoreValueHandler().handle(data, value);
  }

  /**
   * Writes a jsonb array
   *
   * @param value
   * @throws IOException
   */
  public void writeJsonb(String value) throws IOException {
    new JsonbValueHandler().handle(data, value);
  }

  /**
   * Writes a geometry value.
   *
   * @param value
   * @throws IOException
   */
  public void writePostgisGeometry(Geometry value) throws IOException {
    new PostgisGeometryValueHandler().handle(data, value);
  }

  /** Close the writer. */
  @Override
  public void close() throws IOException {
    data.writeShort(-1);
    data.flush();
    data.close();
  }
}
