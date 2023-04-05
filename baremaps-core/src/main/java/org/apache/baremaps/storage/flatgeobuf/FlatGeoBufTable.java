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
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.storage.AbstractTable;
import org.apache.baremaps.storage.Row;
import org.apache.baremaps.storage.Schema;
import org.apache.baremaps.storage.flatgeobuf.internal.TableConversions;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

/**
 * A table that stores rows in a flatgeobuf file.
 */
public class FlatGeoBufTable extends AbstractTable {

  private final Path file;

  private Schema schema;

  /**
   * Constructs a table from a flatgeobuf file (used for reading).
   *
   * @param file the path to the flatgeobuf file
   */
  public FlatGeoBufTable(Path file) {
    this.file = file;
    this.schema = readSchema(file);
  }

  /**
   * Constructs a table from a flatgeobuf file and a schema (used for writing).
   *
   * @param file the path to the flatgeobuf file
   * @param schema the schema of the table
   */
  public FlatGeoBufTable(Path file, Schema schema) {
    this.file = file;
    this.schema = schema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Schema schema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  public static Schema readSchema(Path file) {
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      // try to read the schema from the file
      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      return TableConversions.asFeatureType(headerMeta);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Row> iterator() {
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
      return new RowIterator(channel, headerMeta, schema, buffer);
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
  public void write(Collection<Row> features) throws IOException {
    try (
        var channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        var outputStream = Channels.newOutputStream(channel)) {
      outputStream.write(Constants.MAGIC_BYTES);

      var bufferBuilder = new FlatBufferBuilder();

      var headerMeta = new HeaderMeta();
      headerMeta.geometryType = GeometryType.Unknown;
      headerMeta.indexNodeSize = 16;
      headerMeta.featuresCount =
          features instanceof AbstractDataCollection<Row>c ? c.sizeAsLong() : features.size();
      headerMeta.name = schema.name();
      headerMeta.columns = TableConversions.asColumns(schema.columns());
      HeaderMeta.write(headerMeta, outputStream, bufferBuilder);

      var indexSize =
          (int) PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);

      for (int i = 0; i < indexSize; i++) {
        outputStream.write(0);
      }

      var iterator = features.iterator();
      while (iterator.hasNext()) {
        var row = iterator.next();
        var featureBuilder = new FlatBufferBuilder();
        var geometryOffset = 0;
        var propertiesOffset = 0;
        var propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
        var i = 0;
        for (Object value : row.values()) {
          if (value instanceof Geometry geometry) {
            try {
              geometryOffset =
                  GeometryConversions.serialize(featureBuilder, geometry, headerMeta.geometryType);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } else {
            var column = headerMeta.columns.get(i);
            propertiesBuffer.putShort((short) i);
            TableConversions.writeValue(propertiesBuffer, column, value);
            i++;
          }
        }
        propertiesBuffer.flip();
        propertiesOffset = org.wololo.flatgeobuf.generated.Feature
            .createPropertiesVector(featureBuilder, propertiesBuffer);

        var featureOffset =
            org.wololo.flatgeobuf.generated.Feature.createFeature(featureBuilder, geometryOffset,
                propertiesOffset, 0);

        featureBuilder.finishSizePrefixed(featureOffset);

        channel.write(featureBuilder.dataBuffer());
      }
    }
  }

  /**
   * An iterator over rows in a flatgeobuf file.
   */
  public static class RowIterator implements Iterator<Row> {

    private final HeaderMeta headerMeta;

    private final Schema schema;

    private final SeekableByteChannel channel;

    private final ByteBuffer buffer;

    private long cursor = 0;

    /**
     * Constructs a row iterator.
     *
     * @param channel the channel to read from
     * @param headerMeta the header meta
     * @param schema the schema of the table
     * @param buffer the buffer to use
     */
    public RowIterator(SeekableByteChannel channel, HeaderMeta headerMeta,
        Schema schema, ByteBuffer buffer) {
      this.channel = channel;
      this.headerMeta = headerMeta;
      this.schema = schema;
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
    public Row next() {
      try {
        channel.read(buffer);
        buffer.flip();

        var featureSize = buffer.getInt();
        var row =
            TableConversions.asRow(headerMeta, schema, Feature.getRootAsFeature(buffer));

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
