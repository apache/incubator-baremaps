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

package org.apache.baremaps.database.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.baremaps.stream.StreamUtils;

public class JaggedDataMap<E> extends DataMap<E> {

  private long[][][][] index;

  private final AppendOnlyBuffer<E> values;

  /**
   * Constructs a map.
   *
   * @param values the values
   */
  public JaggedDataMap(AppendOnlyBuffer<E> values) {
    this.index = new long[1 << 12][][][];
    this.values = values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E put(Long key, E value) {
    long v = key;
    int i = (int) (v >>> 28) & 0xFFF;
    int j = (int) (v >>> 16) & 0xFFF;
    int k = (int) (v >>> 8) & 0xFF;
    int l = (int) (v & 0xFF);
    if (index[i] == null) {
      index[i] = new long[1 << 12][][];
    }
    if (index[i][j] == null) {
      index[i][j] = new long[1 << 12][];
    }
    if (index[i][j][k] == null) {
      index[i][j][k] = new long[1 << 8];
      Arrays.fill(index[i][j][k], -1);
    }
    long position = values.addPositioned(value);
    index[i][j][k][l] = position;
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E get(Object key) {
    long v = (Long) key;
    int i = (int) (v >>> 28) & 0xFFF;
    int j = (int) (v >>> 16) & 0xFFF;
    int k = (int) (v >>> 8) & 0xFF;
    int l = (int) (v & 0xFF);
    if (index[i] == null) {
      return null;
    }
    if (index[i][j] == null) {
      return null;
    }
    if (index[i][j][k] == null) {
      return null;
    }
    long position = index[i][j][k][l];
    if (position == -1) {
      return null;
    }
    return values.read(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<Long> keyIterator() {
    return IntStream.range(0, index.length)
        .filter(i -> index[i] != null)
        .mapToObj(i -> IntStream.range(0, index[i].length)
            .filter(j -> index[i][j] != null)
            .mapToObj(j -> IntStream.range(0, index[i][j].length)
                .filter(k -> index[i][j][k] != null)
                .mapToObj(k -> IntStream.range(0, index[i][j][k].length)
                    .filter(l -> index[i][j][k][l] != -1)
                    .mapToObj(l -> (long) ((i << 28) | (j << 16) | (k << 8) | l)))
                .flatMap(x -> x))
            .flatMap(x -> x))
        .flatMap(x -> x)
        .iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<E> valueIterator() {
    return StreamUtils.stream(keyIterator()).map(this::get).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<Entry<Long, E>> entryIterator() {
    return StreamUtils.stream(keyIterator()).map(key -> Map.entry(key, get(key))).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return StreamUtils.stream(keyIterator()).findAny().isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return StreamUtils.stream(keyIterator()).count();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsValue(Object value) {
    return StreamUtils.stream(valueIterator()).anyMatch(v -> v.equals(value));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E remove(Object key) {
    long v = (Long) key;
    int i = (int) (v >>> 28) & 0xFFF;
    int j = (int) (v >>> 16) & 0xFFF;
    int k = (int) (v >>> 8) & 0xFF;
    int l = (int) (v & 0xFF);
    if (index[i] == null) {
      return null;
    }
    if (index[i][j] == null) {
      return null;
    }
    if (index[i][j][k] == null) {
      return null;
    }
    long position = index[i][j][k][l];
    if (position == -1) {
      return null;
    }
    index[i][j][k][l] = -1;
    return values.read(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    index = new long[1 << 12][][][];
  }
}
