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
import java.util.Spliterator;
import java.util.function.Consumer;
import org.apache.baremaps.feature.FeatureType;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.Feature;

public class FeatureIterator implements Iterator<org.apache.baremaps.feature.Feature> {

  private final HeaderMeta headerMeta;

  private final FeatureType featureType;

  private final SeekableByteChannel channel;

  private final ByteBuffer buffer;

  private long cursor = 0;

  public FeatureIterator(SeekableByteChannel channel, HeaderMeta headerMeta,
                         FeatureType featureType) {
    this.channel = channel;
    this.headerMeta = headerMeta;
    this.featureType = featureType;
    this.buffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  public boolean hasNext() {
    return cursor < headerMeta.featuresCount;
  }

  @Override
  public org.apache.baremaps.feature.Feature next() {
    try {
      channel.read(buffer);
      buffer.flip();

      var featureSize = buffer.getInt();
      var feature =
              FeatureConversions.asFeature(headerMeta, featureType, Feature.getRootAsFeature(buffer));

      buffer.position(Integer.BYTES + featureSize);
      buffer.compact();

      cursor++;

      return feature;
    } catch (IOException e) {
      throw new NoSuchElementException(e);
    }
  }
}
