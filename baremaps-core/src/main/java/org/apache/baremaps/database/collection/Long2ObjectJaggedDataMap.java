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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.apache.baremaps.stream.StreamUtils;

/**
 * A map that stores data in a jagged array, using the 40 least significant bits of the key to index
 * the array. The capacity of the map is 2^40, which is sufficient to store 1 trillion entries.
 *
 * @param <E>
 */
public class Long2ObjectJaggedDataMap<E> extends AbstractDataMap<E> {

  private static final int L_BYTES = 8;
  private static final int L_SIZE = 1 << L_BYTES;
  private static final int L_MASK = L_SIZE - 1;
  private static final int L_SHIFT = 0;

  private static final int K_BYTES = 8;
  private static final int K_SIZE = 1 << K_BYTES;
  private static final int K_MASK = K_SIZE - 1;
  private static final int K_SHIFT = L_SHIFT + L_BYTES;

  private static final int J_BYTES = 12;
  private static final int J_SIZE = 1 << J_BYTES;
  private static final int J_MASK = J_SIZE - 1;
  private static final int J_SHIFT = K_SHIFT + K_BYTES;

  private static final int I_BYTES = 12;
  private static final int I_SIZE = 1 << I_BYTES;
  private static final int I_MASK = I_SIZE - 1;
  private static final int I_SHIFT = J_SHIFT + J_BYTES;

  private static final long CAPACITY = 1L << (I_BYTES + J_BYTES + K_BYTES + L_BYTES);

  private long[][][][] index;

  private final AppendOnlyBuffer<E> values;

  private final AtomicLong size = new AtomicLong();

  /**
   * Constructs a jagged data map.
   *
   * @param values the values
   */
  public Long2ObjectJaggedDataMap(AppendOnlyBuffer<E> values) {
    this.index = new long[I_SIZE][][][];
    this.values = values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E put(Long key, E value) {
    long v = key;
    if (v < 0 || v >= CAPACITY) {
      throw new IllegalArgumentException();
    }
    int i = (int) (v >>> I_SHIFT) & I_MASK;
    int j = (int) (v >>> J_SHIFT) & J_MASK;
    int k = (int) (v >>> K_SHIFT) & K_MASK;
    int l = (int) (v >>> L_SHIFT) & L_MASK;
    if (index[i] == null) {
      index[i] = new long[J_SIZE][][];
    }
    if (index[i][j] == null) {
      index[i][j] = new long[K_SIZE][];
    }
    if (index[i][j][k] == null) {
      index[i][j][k] = new long[L_SIZE];
      Arrays.fill(index[i][j][k], -1);
    }
    long position = values.addPositioned(value);
    if (index[i][j][k][l] == -1) {
      size.incrementAndGet();
    }
    index[i][j][k][l] = position;
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E get(Object key) {
    long v = (Long) key;
    if (v < 0 || v >= CAPACITY) {
      throw new IllegalArgumentException();
    }
    int i = (int) (v >>> I_SHIFT) & I_MASK;
    int j = (int) (v >>> J_SHIFT) & J_MASK;
    int k = (int) (v >>> K_SHIFT) & K_MASK;
    int l = (int) (v >>> L_SHIFT) & L_MASK;
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
                    .mapToObj(l -> (long) ((i << I_SHIFT) | (j << J_SHIFT) | (k << K_SHIFT)
                        | (l << L_SHIFT))))
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
  public long size64() {
    return size.get();
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
    int i = (int) (v >>> I_SHIFT) & I_MASK;
    int j = (int) (v >>> J_SHIFT) & J_MASK;
    int k = (int) (v >>> K_SHIFT) & K_MASK;
    int l = (int) (v >>> L_SHIFT) & L_MASK;
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
    size.decrementAndGet();
    index[i][j][k][l] = -1;
    return values.read(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    index = new long[I_SIZE][][][];
  }
}
