/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.database.copy;



import de.bytefish.pgbulkinsert.pgsql.handlers.*;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.copy.PGCopyOutputStream;
import org.postgresql.core.Oid;

/** A helper for writing in a {@code PGCopyOutputStream}. */
public class CopyWriter implements AutoCloseable {

  public static final StringValueHandler STRING_HANDLER =
      new StringValueHandler();

  public static final CollectionValueHandler<String, Collection<String>> STRING_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.TEXT, new StringValueHandler());

  public static final BooleanValueHandler BOOLEAN_HANDLER =
      new BooleanValueHandler();

  public static final CollectionValueHandler<Boolean, Collection<Boolean>> BOOLEAN_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.BOOL, new BooleanValueHandler());

  public static final ByteValueHandler<Number> BYTE_HANDLER =
      new ByteValueHandler<>();

  public static final ByteArrayValueHandler BYTE_ARRAY_HANDLER =
      new ByteArrayValueHandler();

  public static final ShortValueHandler<Number> SHORT_HANDLER =
      new ShortValueHandler<>();

  public static final CollectionValueHandler<Short, Collection<Short>> SHORT_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.INT2, new ShortValueHandler<>());

  public static final IntegerValueHandler<Number> INTEGER_HANDLER =
      new IntegerValueHandler<>();

  public static final CollectionValueHandler<Integer, Collection<Integer>> INTEGER_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.INT4, new IntegerValueHandler<>());

  public static final LongValueHandler<Number> LONG_HANDLER =
      new LongValueHandler<>();

  public static final CollectionValueHandler<Long, Collection<Long>> LONG_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.INT8, new LongValueHandler<>());

  public static final FloatValueHandler<Number> FLOAT_HANDLER =
      new FloatValueHandler<>();

  public static final CollectionValueHandler<Float, Collection<Float>> FLOAT_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.FLOAT4, new FloatValueHandler<>());

  public static final DoubleValueHandler<Number> DOUBLE_HANDLER =
      new DoubleValueHandler<>();

  public static final CollectionValueHandler<Double, Collection<Double>> DOUBLE_COLLECTION_HANDLER =
      new CollectionValueHandler<>(Oid.FLOAT8, new DoubleValueHandler<>());

  public static final LocalDateValueHandler LOCAL_DATE_HANDLER =
      new LocalDateValueHandler();

  public static final LocalDateTimeValueHandler LOCAL_DATE_TIME_HANDLER =
      new LocalDateTimeValueHandler();

  public static final Inet4AddressValueHandler INET_4_ADDRESS_HANDLER =
      new Inet4AddressValueHandler();

  public static final Inet6AddressValueHandler INET_6_ADDRESS_HANDLER =
      new Inet6AddressValueHandler();

  public static final HstoreValueHandler HSTORE_HANDLER =
      new HstoreValueHandler();

  public static final JsonbValueHandler JSONB_HANDLER =
      new JsonbValueHandler();

  public static final GeometryValueHandler GEOMETRY_HANDLER =
      new GeometryValueHandler();

  public static final EnvelopeValueHandler ENVELOPE_HANDLER =
      new EnvelopeValueHandler();

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
    STRING_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of string values.
   *
   * @param value
   * @throws IOException
   */
  public void write(List<String> value) throws IOException {
    STRING_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a boolean value.
   *
   * @param value
   * @throws IOException
   */
  public void writeBoolean(Boolean value) throws IOException {
    BOOLEAN_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of boolean values.
   *
   * @param value
   * @throws IOException
   */
  public void writeBooleanList(List<Boolean> value) throws IOException {
    BOOLEAN_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a byte value.
   *
   * @param value
   * @throws IOException
   */
  public void writeByte(Byte value) throws IOException {
    BYTE_HANDLER.handle(data, value);
  }

  /**
   * Writes a byte array value.
   *
   * @param value
   * @throws IOException
   */
  public void writeByteArray(byte[] value) throws IOException {
    BYTE_ARRAY_HANDLER.handle(data, value);
  }

  /**
   * Writes a short value.
   *
   * @param value
   * @throws IOException
   */
  public void writeShort(Short value) throws IOException {
    SHORT_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of short values.
   *
   * @param value
   * @throws IOException
   */
  public void writeShortList(List<Short> value) throws IOException {
    SHORT_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes an integer value.
   *
   * @param value
   * @throws IOException
   */
  public void writeInteger(Integer value) throws IOException {
    INTEGER_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of integer values.
   *
   * @param value
   * @throws IOException
   */
  public void writeIntegerList(List<Integer> value) throws IOException {
    INTEGER_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a long value.
   *
   * @param value
   * @throws IOException
   */
  public void writeLong(Long value) throws IOException {
    LONG_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of long values.
   *
   * @param value
   * @throws IOException
   */
  public void writeLongList(List<Long> value) throws IOException {
    LONG_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a float value.
   *
   * @param value
   * @throws IOException
   */
  public void writeFloat(Float value) throws IOException {
    FLOAT_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of float values.
   *
   * @param value
   * @throws IOException
   */
  public void writeFloatList(List<Float> value) throws IOException {
    FLOAT_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a double value.
   *
   * @param value
   * @throws IOException
   */
  public void writeDouble(Double value) throws IOException {
    DOUBLE_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of double values.
   *
   * @param value
   * @throws IOException
   */
  public void writeDoubleArray(List<Double> value) throws IOException {
    DOUBLE_COLLECTION_HANDLER.handle(data, value);
  }

  /**
   * Writes a date value.
   *
   * @param value
   * @throws IOException
   */
  public void writeLocalDate(LocalDate value) throws IOException {
    LOCAL_DATE_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of date values.
   *
   * @param value
   * @throws IOException
   */
  public void writeLocalDateTime(LocalDateTime value) throws IOException {
    LOCAL_DATE_TIME_HANDLER.handle(data, value);
  }

  /**
   * Writes an inet adress value.
   *
   * @param value
   * @throws IOException
   */
  public void writeInet4Adress(Inet4Address value) throws IOException {
    INET_4_ADDRESS_HANDLER.handle(data, value);
  }

  /**
   * Writes a list of inet adress values.
   *
   * @param value
   * @throws IOException
   */
  public void writeInet6Adress(Inet6Address value) throws IOException {
    INET_6_ADDRESS_HANDLER.handle(data, value);
  }

  /**
   * Writes a map value.
   *
   * @param value
   * @throws IOException
   */
  public void writeHstore(Map<String, String> value) throws IOException {
    HSTORE_HANDLER.handle(data, value);
  }

  /**
   * Writes a jsonb array
   *
   * @param value
   * @throws IOException
   */
  public void writeJsonb(String value) throws IOException {
    JSONB_HANDLER.handle(data, value);
  }

  /**
   * Writes a geometry value.
   *
   * @param value
   * @throws IOException
   */
  public void writeGeometry(Geometry value) throws IOException {
    GEOMETRY_HANDLER.handle(data, value);
  }

  /**
   * Writes an envelope value.
   *
   * @param value
   * @throws IOException
   */
  public void writeEnvelope(Envelope value) throws IOException {
    ENVELOPE_HANDLER.handle(data, value);
  }

  /** Close the writer. */
  @Override
  public void close() throws IOException {
    data.writeShort(-1);
    data.flush();
    data.close();
  }
}
