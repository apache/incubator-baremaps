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

package org.apache.baremaps.storage.flatgeobuf;

import com.google.flatbuffers.FlatBufferBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.baremaps.database.collection.AbstractDataCollection;
import org.apache.baremaps.database.schema.AbstractDataTable;
import org.apache.baremaps.database.schema.DataRow;
import org.apache.baremaps.database.schema.DataRowType;
import org.locationtech.jts.geom.*;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

/**
 * A table that stores rows in a flatgeobuf file.
 */
public class FlatGeoBufDataTable extends AbstractDataTable {

  private final Path file;

  private DataRowType rowType;

  /**
   * Constructs a table from a flatgeobuf file (used for reading).
   *
   * @param file the path to the flatgeobuf file
   */
  public FlatGeoBufDataTable(Path file) {
    this.file = file;
    this.rowType = readRowType(file);
  }

  /**
   * Constructs a table from a flatgeobuf file and a row type (used for writing).
   *
   * @param file the path to the flatgeobuf file
   * @param rowType the row type of the table
   */
  public FlatGeoBufDataTable(Path file, DataRowType rowType) {
    this.file = file;
    this.rowType = rowType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRowType rowType() {
    return rowType;
  }

  /**
   * {@inheritDoc}
   */
  public static DataRowType readRowType(Path file) {
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      // try to read the row type from the file
      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      return FlatGeoBufTypeConversion.asRowType(headerMeta);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    try {
      var channel = FileChannel.open(file, StandardOpenOption.READ);

      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      channel.position(headerMeta.offset);

      // skip the index
      long indexOffset = headerMeta.offset;
      long indexSize =
          PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);
      channel.position(indexOffset + indexSize);

      buffer.clear();

      // create the feature stream
      return new RowIterator(channel, headerMeta, rowType, buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      return headerMeta.featuresCount;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads the header meta from a channel.
   *
   * @param channel the channel to read from
   * @param buffer the buffer to use
   * @return the header meta
   * @throws IOException if an error occurs while reading the header meta
   */
  private static HeaderMeta readHeaderMeta(SeekableByteChannel channel, ByteBuffer buffer)
      throws IOException {
    channel.read(buffer);
    buffer.flip();
    return HeaderMeta.read(buffer);
  }

  /**
   * Writes a collection of rows to a flatgeobuf file.
   *
   * @param features the collection of rows to write
   * @throws IOException if an error occurs while writing the rows
   */
  public void write(Collection<DataRow> features) throws IOException {
    try (
        var channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        var outputStream = Channels.newOutputStream(channel)) {
      outputStream.write(Constants.MAGIC_BYTES);

      var bufferBuilder = new FlatBufferBuilder();

      var headerMeta = new HeaderMeta();
      headerMeta.geometryType = GeometryType.Unknown;
      headerMeta.indexNodeSize = 16;
      headerMeta.srid = 3857;
      headerMeta.featuresCount =
          features instanceof AbstractDataCollection<DataRow>c ? c.sizeAsLong() : features.size();
      headerMeta.name = rowType.name();
      headerMeta.columns = FlatGeoBufTypeConversion.asColumns(rowType.columns());
      HeaderMeta.write(headerMeta, outputStream, bufferBuilder);

      var indexSize =
          (int) PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);

      for (int i = 0; i < indexSize; i++) {
        outputStream.write(0);
      }

      var iterator = features.iterator();
      while (iterator.hasNext()) {
        var featureBuilder = new FlatBufferBuilder(4096);

        var row = iterator.next();

        var propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
        var properties = row.values().stream()
            .filter(v -> !(v instanceof Geometry))
            .toList();
        for (int i = 0; i < properties.size(); i++) {
          var column = headerMeta.columns.get(i);
          var value = properties.get(i);
          propertiesBuffer.putShort((short) i);
          FlatGeoBufTypeConversion.writeValue(propertiesBuffer, column, value);
        }
        if (propertiesBuffer.position() > 0) {
          propertiesBuffer.flip();
        }
        var propertiesOffset = org.wololo.flatgeobuf.generated.Feature
            .createPropertiesVector(featureBuilder, propertiesBuffer);

        var geometry = row.values().stream()
            .filter(v -> v instanceof Geometry)
            .map(Geometry.class::cast)
            .findFirst();

        var geometryOffset = geometry.isPresent()
            ? GeometryConversions.serialize(featureBuilder, geometry.get(), headerMeta.geometryType)
            : 0;

        var featureOffset =
            org.wololo.flatgeobuf.generated.Feature.createFeature(featureBuilder, geometryOffset,
                propertiesOffset, 0);
        featureBuilder.finishSizePrefixed(featureOffset);

        ByteBuffer data = featureBuilder.dataBuffer();
        while (data.hasRemaining()) {
          channel.write(data);
        }
      }
    }
  }

  /**
   * An iterator over rows in a flatgeobuf file.
   */
  public static class RowIterator implements Iterator<DataRow> {

    private final HeaderMeta headerMeta;

    private final DataRowType rowType;

    private final SeekableByteChannel channel;

    private final ByteBuffer buffer;

    private long cursor = 0;

    /**
     * Constructs a row iterator.
     *
     * @param channel the channel to read from
     * @param headerMeta the header meta
     * @param rowType the row type of the table
     * @param buffer the buffer to use
     */
    public RowIterator(SeekableByteChannel channel, HeaderMeta headerMeta,
        DataRowType rowType, ByteBuffer buffer) {
      this.channel = channel;
      this.headerMeta = headerMeta;
      this.rowType = rowType;
      this.buffer = buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      return cursor < headerMeta.featuresCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataRow next() {
      try {
        channel.read(buffer);
        buffer.flip();

        var featureSize = buffer.getInt();
        var row =
            FlatGeoBufTypeConversion.asRow(headerMeta, rowType, Feature.getRootAsFeature(buffer));

        buffer.position(Integer.BYTES + featureSize);
        buffer.compact();

        cursor++;

        return row;
      } catch (IOException e) {
        throw new NoSuchElementException(e);
      }
    }
  }
}
