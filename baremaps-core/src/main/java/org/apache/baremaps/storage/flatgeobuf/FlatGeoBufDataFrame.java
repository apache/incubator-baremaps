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
import org.apache.baremaps.dataframe.Row;
import org.apache.baremaps.dataframe.Schema;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

public class FlatGeoBufDataFrame extends AbstractDataCollection<Row> {

  private final Path file;

  private Schema schema;

  public FlatGeoBufDataFrame(Path file) {
    this.file = file;
  }

  public FlatGeoBufDataFrame(Path file, Schema schema) {
    this.file = file;
    this.schema = schema;
  }

  public Schema getSchema() throws IOException {
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      return DataFrameConversions.asFeatureType(headerMeta);
    }
  }

  @Override
  public Iterator<Row> iterator() {
    try {
      var channel = FileChannel.open(file, StandardOpenOption.READ);

      var buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
      HeaderMeta headerMeta = readHeaderMeta(channel, buffer);
      channel.position(headerMeta.offset);

      // skip the index
      var indexSize =
          (int) PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);
      channel.position(headerMeta.offset + indexSize);

      buffer.clear();

      // create the feature stream
      return new RowIterator(channel, headerMeta, schema, buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private HeaderMeta readHeaderMeta(SeekableByteChannel channel, ByteBuffer buffer)
      throws IOException {
    channel.read(buffer);
    buffer.flip();
    return HeaderMeta.read(buffer);
  }

  public void write(Collection<Row> features) throws IOException {
    try (
        var channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

      var outputStream = Channels.newOutputStream(channel);
      outputStream.write(Constants.MAGIC_BYTES);

      var bufferBuilder = new FlatBufferBuilder();

      var headerMeta = new HeaderMeta();
      headerMeta.geometryType = GeometryType.Unknown;
      headerMeta.indexNodeSize = 16;
      headerMeta.featuresCount =
          features instanceof AbstractDataCollection<Row>c ? c.sizeAsLong() : features.size();
      headerMeta.name = schema.name();
      headerMeta.columns = DataFrameConversions.asColumns(schema.columns());
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
            DataFrameConversions.writeValue(propertiesBuffer, column, value);
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

  public static class RowIterator implements Iterator<Row> {

    private final HeaderMeta headerMeta;

    private final Schema dataType;

    private final SeekableByteChannel channel;

    private final ByteBuffer buffer;

    private long cursor = 0;

    public RowIterator(SeekableByteChannel channel, HeaderMeta headerMeta,
        Schema dataType, ByteBuffer buffer) {
      this.channel = channel;
      this.headerMeta = headerMeta;
      this.dataType = dataType;
      this.buffer = buffer;
    }

    @Override
    public boolean hasNext() {
      return cursor < headerMeta.featuresCount;
    }

    @Override
    public Row next() {
      try {
        channel.read(buffer);
        buffer.flip();

        var featureSize = buffer.getInt();
        var row =
            DataFrameConversions.asRow(headerMeta, dataType, Feature.getRootAsFeature(buffer));

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
