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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.baremaps.dataframe.Row;
import org.apache.baremaps.dataframe.Schema;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.Feature;

public class RowIterator implements Iterator<Row> {

  private final HeaderMeta headerMeta;

  private final Schema dataType;

  private final SeekableByteChannel channel;

  private final ByteBuffer buffer;

  private long cursor = 0;

  public RowIterator(SeekableByteChannel channel, HeaderMeta headerMeta,
      Schema dataType) {
    this.channel = channel;
    this.headerMeta = headerMeta;
    this.dataType = dataType;
    this.buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
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
          RowConversions.asRow(headerMeta, dataType, Feature.getRootAsFeature(buffer));

      buffer.position(Integer.BYTES + featureSize);
      buffer.compact();

      cursor++;

      return row;
    } catch (IOException e) {
      throw new NoSuchElementException(e);
    }
  }
}
