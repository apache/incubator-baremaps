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

import static it.unimi.dsi.fastutil.HashCommon.*;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * An open addressed hash map of long keys and long values derived from fastutil's
 * {@link Long2LongOpenHashMap}. This implementation allows for the use of on-heap, off-heap, or
 * memory mapped memory.
 */
public class Long2LongOpenHashDataMap extends AbstractLong2LongMap
    implements DataMap<Long>, Hash {

  protected Supplier<AbstractDataList<Long>> keySupplier;

  protected Supplier<AbstractDataList<Long>> valueSupplier;

  /**
   * The array of keys.
   */
  protected AbstractDataList<Long> key;
  /**
   * The array of values.
   */
  protected AbstractDataList<Long> value;
  /**
   * The mask for wrapping a position counter.
   */
  protected long mask;
  /**
   * Whether this map contains the key zero.
   */
  protected boolean containsNullKey;
  /**
   * The current table size.
   */
  protected long n;
  /**
   * Threshold after which we rehash. It must be the table size times {@link #f}.
   */
  protected long maxFill;
  /**
   * We never resize below this threshold, which is the construction-time {#n}.
   */
  protected final long minN;
  /**
   * Number of entries in the set (including the key zero, if present).
   */
  protected AtomicLong size = new AtomicLong();
  /**
   * The acceptable load factor.
   */
  protected final float f;
  /**
   * Cached set of entries.
   */
  protected FastEntrySet entries;

  /**
   * Cached set of keys.
   */
  protected LongSet keys;
  /**
   * Cached collection of values.
   */
  protected LongCollection values;

  /**
   * Creates a new hash map.
   *
   * <p>
   * The actual table size will be the least power of two greater than {@code expected}/{@code f}.
   *
   * @param expected the expected number of elements in the hash map.
   * @param f the load factor.
   */
  public Long2LongOpenHashDataMap(
      long expected,
      float f,
      Supplier<AbstractDataList<Long>> keySupplier,
      Supplier<AbstractDataList<Long>> valueSupplier) {
    if (f <= 0 || f >= 1) {
      throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
    }
    if (expected < 0) {
      throw new IllegalArgumentException("The expected number of elements must be non-negative");
    }
    this.f = f;
    this.minN = n = bigArraySize(expected, f);
    this.mask = n - 1;
    this.maxFill = maxFill(n, f);
    this.keySupplier = keySupplier;
    this.valueSupplier = valueSupplier;
    this.key = keySupplier.get();
    this.value = valueSupplier.get();
  }

  private long realSize() {
    return containsNullKey ? size.get() - 1 : size.get();
  }

  private void ensureCapacity(final long capacity) {
    final long needed = bigArraySize(capacity, f);
    if (needed > n) {
      rehash(needed);
    }
  }

  private void tryCapacity(final long capacity) {
    final long needed =
        Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / f))));
    if (needed > n) {
      rehash(needed);
    }
  }

  private long removeEntry(final long pos) {
    final long oldValue = value.get(pos);
    size.decrementAndGet();
    shiftKeys(pos);
    if (n > minN && size.get() < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
      rehash(n / 2);
    }
    return oldValue;
  }

  private long removeNullEntry() {
    containsNullKey = false;
    final long oldValue = value.get(n);
    size.decrementAndGet();
    if (n > minN && size.get() < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
      rehash(n / 2);
    }
    return oldValue;
  }

  @Override
  public void putAll(Map<? extends Long, ? extends Long> m) {
    if (f <= .5) {
      ensureCapacity(m.size());
    } else {
      tryCapacity(size64() + m.size());
    }
    super.putAll(m);
  }

  private long find(final long k) {
    if (((k) == 0)) {
      return containsNullKey ? n : -(n + 1);
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return -(pos + 1);
    }
    if (((k) == (curr))) {
      return pos;
    }
    // There's always an unused entry.
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return -(pos + 1);
      }
      if (((k) == (curr))) {
        return pos;
      }
    }
  }

  private void insert(final long pos, final long k, final long v) {
    if (pos == n) {
      containsNullKey = true;
    }
    key.set(pos, k);
    value.set(pos, v);
    if (size.getAndIncrement() >= maxFill) {
      rehash(bigArraySize(size.get() + 1, f));
    }
  }

  @Override
  public long put(final long k, final long v) {
    final long pos = find(k);
    if (pos < 0) {
      insert(-pos - 1, k, v);
      return defRetValue;
    }
    final long oldValue = value.get(pos);
    value.set(pos, v);
    return oldValue;
  }

  private long addToValue(final long pos, final long incr) {
    final long oldValue = value.get(pos);
    value.set(pos, oldValue + incr);
    return oldValue;
  }

  /**
   * Adds an increment to value currently associated with a key.
   *
   * <p>
   * Note that this method respects the {@linkplain #defaultReturnValue() default return value}
   * semantics: when called with a key that does not currently appears in the map, the key will be
   * associated with the default return value plus the given increment.
   *
   * @param k the key.
   * @param incr the increment.
   * @return the old value, or the {@linkplain #defaultReturnValue() default return value} if no
   *         value was present for the given key.
   */
  public long addTo(final long k, final long incr) {
    long pos;
    if (((k) == 0)) {
      if (containsNullKey) {
        return addToValue(n, incr);
      }
      pos = n;
      containsNullKey = true;
    } else {
      long curr;
      // The starting point.
      if (!((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
        if (((curr) == (k))) {
          return addToValue(pos, incr);
        }
        while (!((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
          if (((curr) == (k))) {
            return addToValue(pos, incr);
          }
        }
      }
    }
    key.set(pos, k);
    value.set(pos, defRetValue + incr);
    if (size.incrementAndGet() >= maxFill)
      rehash(bigArraySize(size.get() + 1, f));
    return defRetValue;
  }

  /**
   * Shifts left entries with the specified hash code, starting at the specified position, and
   * empties the resulting free entry.
   *
   * @param pos a starting position.
   */
  protected final void shiftKeys(long pos) {
    // Shift entries with the same hash.
    long last, slot;
    long curr;
    for (;;) {
      pos = ((last = pos) + 1) & mask;
      for (;;) {
        if (((curr = key.get(pos)) == 0)) {
          key.set(last, 0L);
          return;
        }
        slot = HashCommon.mix((curr)) & mask;
        if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
          break;
        }
        pos = (pos + 1) & mask;
      }
      key.set(last, curr);
      value.set(last, value.get(pos));
    }
  }

  @Override

  public long remove(final long k) {
    if (((k) == 0)) {
      if (containsNullKey) {
        return removeNullEntry();
      }
      return defRetValue;
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return defRetValue;
    }
    if (((k) == (curr))) {
      return removeEntry(pos);
    }
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return defRetValue;
      }
      if (((k) == (curr))) {
        return removeEntry(pos);
      }
    }
  }

  @Override

  public long get(final long k) {
    if (((k) == 0)) {
      return containsNullKey ? value.get(n) : defRetValue;
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return defRetValue;
    }
    if (((k) == (curr))) {
      return value.get(pos);
    }
    // There's always an unused entry.
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return defRetValue;
      }
      if (((k) == (curr))) {
        return value.get(pos);
      }
    }
  }

  @Override

  public boolean containsKey(final long k) {
    if (((k) == 0)) {
      return containsNullKey;
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return false;
    }
    if (((k) == (curr))) {
      return true;
    }
    // There's always an unused entry.
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return false;
      }
      if (((k) == (curr))) {
        return true;
      }
    }
  }

  @Override
  public boolean containsValue(final long v) {
    if (containsNullKey && ((value.get(n)) == (v))) {
      return true;
    }
    for (long i = n; i-- != 0;) {
      if (!((key.get(i)) == 0) && ((value.get(i)) == (v))) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public long getOrDefault(final long k, final long defaultValue) {
    if (((k) == 0)) {
      return containsNullKey ? value.get(n) : defaultValue;
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return defaultValue;
    }
    if (((k) == (curr))) {
      return value.get(pos);
    }
    // There's always an unused entry.
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return defaultValue;
      }
      if (((k) == (curr))) {
        return value.get(pos);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long putIfAbsent(final long k, final long v) {
    final long pos = find(k);
    if (pos >= 0) {
      return value.get(pos);
    }
    insert(-pos - 1, k, v);
    return defRetValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public boolean remove(final long k, final long v) {
    if (((k) == 0)) {
      if (containsNullKey && ((v) == (value.get(n)))) {
        removeNullEntry();
        return true;
      }
      return false;
    }
    long curr;
    long pos;
    // The starting point.
    if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
      return false;
    }
    if (((k) == (curr)) && ((v) == (value.get(pos)))) {
      removeEntry(pos);
      return true;
    }
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return false;
      }
      if (((k) == (curr)) && ((v) == (value.get(pos)))) {
        removeEntry(pos);
        return true;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean replace(final long k, final long oldValue, final long v) {
    final long pos = find(k);
    if (pos < 0 || !(oldValue == (value.get(pos)))) {
      return false;
    }
    value.set(pos, v);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long replace(final long k, final long v) {
    final long pos = find(k);
    if (pos < 0) {
      return defRetValue;
    }
    final long oldValue = value.get(pos);
    value.set(pos, v);
    return oldValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long computeIfAbsent(final long k,
      final java.util.function.LongUnaryOperator mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    final long pos = find(k);
    if (pos >= 0) {
      return value.get(pos);
    }
    final long newValue = mappingFunction.applyAsLong(k);
    insert(-pos - 1, k, newValue);
    return newValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long computeIfAbsent(final long key, final Long2LongFunction mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    final long pos = find(key);
    if (pos >= 0) {
      return value.get(pos);
    }
    if (!mappingFunction.containsKey(key)) {
      return defRetValue;
    }
    final long newValue = mappingFunction.get(key);
    insert(-pos - 1, key, newValue);
    return newValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long computeIfAbsentNullable(final long k,
      final java.util.function.LongFunction<? extends Long> mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    final long pos = find(k);
    if (pos >= 0) {
      return value.get(pos);
    }
    final Long newValue = mappingFunction.apply(k);
    if (newValue == null) {
      return defRetValue;
    }
    final long v = newValue;
    insert(-pos - 1, k, v);
    return v;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long computeIfPresent(
      final long k,
      final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    final long pos = find(k);
    if (pos < 0) {
      return defRetValue;
    }
    final Long newValue = remappingFunction.apply(k, value.get(pos));
    if (newValue == null) {
      if (((k) == 0)) {
        removeNullEntry();
      } else {
        removeEntry(pos);
      }
      return defRetValue;
    }
    long newVal = newValue;
    value.set(pos, newVal);
    return newVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long compute(
      final long k,
      final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    final long pos = find(k);
    final Long newValue =
        remappingFunction.apply(k, pos >= 0 ? value.get(pos) : null);
    if (newValue == null) {
      if (pos >= 0) {
        if (((k) == 0)) {
          removeNullEntry();
        } else {
          removeEntry(pos);
        }
      }
      return defRetValue;
    }
    long newVal = newValue;
    if (pos < 0) {
      insert(-pos - 1, k, newVal);
      return newVal;
    }
    value.set(pos, newVal);
    return newVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long merge(final long k, final long v,
      final java.util.function.BiFunction<? super Long, ? super Long, ? extends Long> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    final long pos = find(k);
    if (pos < 0) {
      insert(-pos - 1, k, v);
      return v;
    }
    final Long newValue = remappingFunction.apply(value.get(pos), v);
    if (newValue == null) {
      if (((k) == 0)) {
        removeNullEntry();
      } else {
        removeEntry(pos);
      }
      return defRetValue;
    }
    long newVal = newValue;
    value.set(pos, newVal);
    return newVal;
  }

  /*
   * Removes all elements from this map.
   *
   * <p>To increase object reuse, this method does not change the table size. If you want to reduce
   * the table size, you must use {@link #trim()}.
   *
   */
  @Override
  public void clear() {
    if (size.get() == 0) {
      return;
    }
    size.set(0);
    containsNullKey = false;
  }

  @Override
  public boolean isEmpty() {
    return size.get() == 0;
  }

  @Override
  public long size64() {
    return size.get();
  }

  @Override
  public int size() {
    return (int) Math.min(size64(), Integer.MAX_VALUE);
  }

  /**
   * The entry class for a hash map does not record key and value, but rather the position in the
   * hash table of the corresponding entry. This is necessary so that calls to
   * {@link java.util.Map.Entry#setValue(Object)} are reflected in the map
   */
  final class MapEntry implements Long2LongMap.Entry, Map.Entry<Long, Long>, LongLongPair {
    // The table index this entry refers to, or -1 if this entry has been deleted.
    long index;

    MapEntry(final long index) {
      this.index = index;
    }

    MapEntry() {}

    @Override
    public long getLongKey() {
      return key.get(index);
    }

    @Override
    public long leftLong() {
      return key.get(index);
    }

    @Override
    public long getLongValue() {
      return value.get(index);
    }

    @Override
    public long rightLong() {
      return value.get(index);
    }

    @Override
    public long setValue(final long v) {
      final long oldValue = value.get(index);
      value.set(index, v);
      return oldValue;
    }

    @Override
    public LongLongPair right(final long v) {
      value.set(index, v);
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    @Override
    public Long getKey() {
      return key.get(index);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    @Override
    public Long getValue() {
      return value.get(index);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    @Override
    public Long setValue(final Long v) {
      return setValue((v).longValue());
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<Long, Long> e = (Map.Entry<Long, Long>) o;
      return ((key.get(index)) == ((e.getKey()).longValue()))
          && ((value.get(index)) == ((e.getValue()).longValue()));
    }

    @Override
    public int hashCode() {
      return HashCommon.long2int(key.get(index)) ^ HashCommon.long2int(value.get(index));
    }

    @Override
    public String toString() {
      return key.get(index) + "=>" + value.get(index);
    }
  }

  /**
   * An iterator over a hash map.
   */
  private abstract class MapIterator<ConsumerType> {
    /**
     * The index of the last entry returned, if positive or zero; initially, {@link #n}. If
     * negative, the last entry returned was that of the key of index {@code - pos - 1} from the
     * {@link #wrapped} list.
     */
    long pos = n;
    /**
     * The index of the last entry that has been returned (more precisely, the value of {@link #pos}
     * if {@link #pos} is positive, or {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is
     * -1 if either we did not return an entry yet, or the last returned entry has been removed.
     */
    long last = -1;
    /**
     * A downward counter measuring how many entries must still be returned.
     */
    long c = size.get();
    /**
     * A boolean telling us whether we should return the entry with the null key.
     */
    boolean mustReturnNullKey = Long2LongOpenHashDataMap.this.containsNullKey;
    /**
     * A lazily allocated list containing keys of entries that have wrapped around the table because
     * of removals.
     */
    LongArrayList wrapped;

    abstract void acceptOnIndex(final ConsumerType action, final long index);

    public boolean hasNext() {
      return c != 0;
    }

    public long nextEntry() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      c--;
      if (mustReturnNullKey) {
        mustReturnNullKey = false;
        return last = n;
      }
      for (;;) {
        if (--pos < 0) {
          // We are just enumerating elements from the wrapped list.
          last = Integer.MIN_VALUE;
          final long k = wrapped.getLong((int) -pos - 1);
          long p = (int) HashCommon.mix((k)) & mask;
          while (!(k == (key.get(p)))) {
            p = (p + 1) & mask;
          }
          return p;
        }
        if (!(key.get(pos) == 0)) {
          return last = pos;
        }
      }
    }

    public void forEachRemaining(final ConsumerType action) {
      if (mustReturnNullKey) {
        mustReturnNullKey = false;
        acceptOnIndex(action, last = n);
        c--;
      }
      while (c != 0) {
        if (--pos < 0) {
          // We are just enumerating elements from the wrapped list.
          last = Integer.MIN_VALUE;
          final long k = wrapped.getLong((int) -pos - 1); // TODO: check if -pos - 1 is correct
          long p = (int) HashCommon.mix((k)) & mask;
          while (!(k == (key.get(p)))) {
            p = (p + 1) & mask;
          }
          acceptOnIndex(action, p);
          c--;
        } else if (!(key.get(pos) == 0)) {
          acceptOnIndex(action, last = pos);
          c--;
        }
      }
    }

    /**
     * Shifts left entries with the specified hash code, starting at the specified position, and
     * empties the resulting free entry.
     *
     * @param pos a starting position.
     */
    private void shiftKeys(long pos) {
      // Shift entries with the same hash.
      long last, slot;
      long curr;
      for (;;) {
        pos = ((last = pos) + 1) & mask;
        for (;;) {
          if (((curr = key.get(pos)) == 0)) {
            key.set(last, 0L);
            return;
          }
          slot = (int) HashCommon.mix((curr)) & mask;
          if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
            break;
          }
          pos = (pos + 1) & mask;
        }
        if (pos < last) { // Wrapped entry.
          if (wrapped == null) {
            wrapped = new LongArrayList(2);
          }
          wrapped.add(key.get(pos));
        }
        key.set(last, curr);
        value.set(last, value.get(pos));
      }
    }

    public void remove() {
      if (last == -1) {
        throw new IllegalStateException();
      }
      if (last == n) {
        containsNullKey = false;
      } else if (pos >= 0) {
        shiftKeys(last);
      } else {
        // We're removing wrapped entries.
        Long2LongOpenHashDataMap.this.remove(wrapped.getLong((int) -pos - 1));
        last = -1; // Note that we must not decrement size
        return;
      }
      size.decrementAndGet();
      last = -1; // You can no longer remove this entry.
    }

    public int skip(final int n) {
      int i = n;
      while (i-- != 0 && hasNext()) {
        nextEntry();
      }
      return n - i - 1;
    }
  }

  private final class EntryIterator
      extends Long2LongOpenHashDataMap.MapIterator<Consumer<? super Long2LongMap.Entry>>
      implements ObjectIterator<Long2LongMap.Entry> {
    private Long2LongOpenHashDataMap.MapEntry entry;

    @Override
    public Long2LongOpenHashDataMap.MapEntry next() {
      return entry = new Long2LongOpenHashDataMap.MapEntry(nextEntry());
    }

    // forEachRemaining inherited from MapIterator superclass.
    @Override
    void acceptOnIndex(final Consumer<? super Long2LongMap.Entry> action, final long index) {
      action.accept(entry = new Long2LongOpenHashDataMap.MapEntry(index));
    }

    @Override
    public void remove() {
      super.remove();
      entry.index = -1; // You cannot use a deleted entry.
    }
  }

  private final class FastEntryIterator
      extends Long2LongOpenHashDataMap.MapIterator<Consumer<? super Long2LongMap.Entry>>
      implements ObjectIterator<Long2LongMap.Entry> {
    private final Long2LongOpenHashDataMap.MapEntry entry = new Long2LongOpenHashDataMap.MapEntry();

    @Override
    public Long2LongOpenHashDataMap.MapEntry next() {
      entry.index = nextEntry();
      return entry;
    }

    // forEachRemaining inherited from MapIterator superclass.
    @Override
    void acceptOnIndex(final Consumer<? super Long2LongMap.Entry> action, final long index) {
      entry.index = index;
      action.accept(entry);
    }
  }

  private abstract class MapSpliterator<ConsumerType, SplitType extends Long2LongOpenHashDataMap.MapSpliterator<ConsumerType, SplitType>> {
    /**
     * The index (which bucket) of the next item to give to the action. Unlike {@code SetIterator},
     * this counts up instead of down.
     */
    long pos = 0;
    /**
     * The maximum bucket (exclusive) to iterate to
     */
    long max = n;
    /**
     * An upwards counter counting how many we have given
     */
    long c = 0;
    /**
     * A boolean telling us whether we should return the null key.
     */
    boolean mustReturnNull = Long2LongOpenHashDataMap.this.containsNullKey;
    boolean hasSplit = false;

    MapSpliterator() {}

    MapSpliterator(long pos, long max, boolean mustReturnNull, boolean hasSplit) {
      this.pos = pos;
      this.max = max;
      this.mustReturnNull = mustReturnNull;
      this.hasSplit = hasSplit;
    }

    abstract void acceptOnIndex(final ConsumerType action, final long index);

    abstract SplitType makeForSplit(long pos, long max, boolean mustReturnNull);

    public boolean tryAdvance(final ConsumerType action) {
      if (mustReturnNull) {
        mustReturnNull = false;
        ++c;
        acceptOnIndex(action, n);
        return true;
      }
      while (pos < max) {
        if (!(key.get(pos) == 0)) {
          ++c;
          acceptOnIndex(action, pos++);
          return true;
        }
        ++pos;
      }
      return false;
    }

    public void forEachRemaining(final ConsumerType action) {
      if (mustReturnNull) {
        mustReturnNull = false;
        ++c;
        acceptOnIndex(action, n);
      }
      while (pos < max) {
        if (!(key.get(pos) == 0)) {
          acceptOnIndex(action, pos);
          ++c;
        }
        ++pos;
      }
    }

    public long estimateSize() {
      if (!hasSplit) {
        // Root spliterator; we know how many are remaining.
        return size.get() - c;
      } else {
        // After we split, we can no longer know exactly how many we have (or at least not
        // efficiently).
        // (size / n) * (max - pos) aka currentTableDensity * numberOfBucketsLeft seems like a good
        // estimate.
        return Math.min(size.get() - c,
            (long) (((double) realSize() / n) * (max - pos)) + (mustReturnNull ? 1 : 0));
      }
    }

    public SplitType trySplit() {
      if (pos >= max - 1) {
        return null;
      }
      long retLen = (max - pos) >> 1;
      if (retLen <= 1) {
        return null;
      }
      long myNewPos = pos + retLen;
      long retPos = pos;
      long retMax = myNewPos;
      // Since null is returned first, and the convention is that the returned split is the prefix
      // of
      // elements,
      // the split will take care of returning null (if needed), and we won't return it anymore.
      SplitType split = makeForSplit(retPos, retMax, mustReturnNull);
      this.pos = myNewPos;
      this.mustReturnNull = false;
      this.hasSplit = true;
      return split;
    }

    public long skip(long n) {
      if (n < 0) {
        throw new IllegalArgumentException("Argument must be nonnegative: " + n);
      }
      if (n == 0) {
        return 0;
      }
      long skipped = 0;
      if (mustReturnNull) {
        mustReturnNull = false;
        ++skipped;
        --n;
      }
      while (pos < max && n > 0) {
        if (!(key.get(pos++) == 0)) {
          ++skipped;
          --n;
        }
      }
      return skipped;
    }
  }

  private final class EntrySpliterator extends
      Long2LongOpenHashDataMap.MapSpliterator<Consumer<? super Long2LongMap.Entry>, Long2LongOpenHashDataMap.EntrySpliterator>
      implements ObjectSpliterator<Long2LongMap.Entry> {
    private static final int POST_SPLIT_CHARACTERISTICS =
        ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

    EntrySpliterator() {}

    EntrySpliterator(long pos, long max, boolean mustReturnNull, boolean hasSplit) {
      super(pos, max, mustReturnNull, hasSplit);
    }

    @Override
    public int characteristics() {
      return hasSplit ? POST_SPLIT_CHARACTERISTICS
          : ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS;
    }

    @Override
    void acceptOnIndex(final Consumer<? super Long2LongMap.Entry> action, final long index) {
      action.accept(new Long2LongOpenHashDataMap.MapEntry(index));
    }

    @Override
    Long2LongOpenHashDataMap.EntrySpliterator makeForSplit(long pos, long max,
        boolean mustReturnNull) {
      return new Long2LongOpenHashDataMap.EntrySpliterator(pos, max, mustReturnNull, true);
    }
  }

  private final class MapEntrySet extends AbstractObjectSet<Long2LongMap.Entry>
      implements FastEntrySet {
    @Override
    public ObjectIterator<Long2LongMap.Entry> iterator() {
      return new Long2LongOpenHashDataMap.EntryIterator();
    }

    @Override
    public int size() {
      return (int) Math.min(size64(), Integer.MAX_VALUE);
    }

    @Override
    public ObjectIterator<Long2LongMap.Entry> fastIterator() {
      return new Long2LongOpenHashDataMap.FastEntryIterator();
    }

    @Override
    public ObjectSpliterator<Long2LongMap.Entry> spliterator() {
      return new Long2LongOpenHashDataMap.EntrySpliterator();
    }

    //
    @Override

    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      if (e.getKey() == null || !(e.getKey() instanceof Long)) {
        return false;
      }
      if (e.getValue() == null || !(e.getValue() instanceof Long)) {
        return false;
      }
      final long k = (Long) (e.getKey());
      final long v = (Long) (e.getValue());
      if (((k) == 0)) {
        return Long2LongOpenHashDataMap.this.containsNullKey && ((value.get(n)) == (v));
      }
      long curr;
      long pos;
      // The starting point.
      if (((curr = key.get(pos = (int) HashCommon.mix((k)) & mask)) == 0)) {
        return false;
      }
      if (((k) == (curr))) {
        return ((value.get(pos)) == (v));
      }
      // There's always an unused entry.
      while (true) {
        if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
          return false;
        }
        if (((k) == (curr))) {
          return ((value.get(pos)) == (v));
        }
      }
    }

    @Override
    public boolean remove(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      if (e.getKey() == null || !(e.getKey() instanceof Long)) {
        return false;
      }
      if (e.getValue() == null || !(e.getValue() instanceof Long)) {
        return false;
      }
      final long k = (Long) e.getKey();
      final long v = (Long) e.getValue();
      if (((k) == 0)) {
        if (containsNullKey && ((value.get(n)) == (v))) {
          removeNullEntry();
          return true;
        }
        return false;
      }
      long curr;
      long pos;
      // The starting point.
      if (((curr = key.get(pos = (int) HashCommon.mix((k)) & mask)) == 0)) {
        return false;
      }
      if (((curr) == (k))) {
        if (((value.get(pos)) == (v))) {
          removeEntry(pos);
          return true;
        }
        return false;
      }
      while (true) {
        if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
          return false;
        }
        if (((curr) == (k))) {
          if (((value.get(pos)) == (v))) {
            removeEntry(pos);
            return true;
          }
        }
      }
    }

    @Override
    public void clear() {
      Long2LongOpenHashDataMap.this.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(final Consumer<? super Long2LongMap.Entry> consumer) {
      if (containsNullKey) {
        consumer.accept(new AbstractLong2LongMap.BasicEntry(key.get(n), value.get(n)));
      }
      for (long pos = n; pos-- != 0;) {
        if (!(key.get(pos) == 0)) {
          consumer.accept(new AbstractLong2LongMap.BasicEntry(key.get(pos), value.get(pos)));
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fastForEach(final Consumer<? super Long2LongMap.Entry> consumer) {
      if (containsNullKey) {
        consumer.accept(new AbstractLong2LongMap.BasicEntry(key.get(n), value.get(n)));
      }
      for (long pos = n; pos-- != 0;) {
        if (!(key.get(pos) == 0)) {
          consumer.accept(new AbstractLong2LongMap.BasicEntry(key.get(pos), value.get(pos)));
        }
      }
    }
  }

  @Override
  public FastEntrySet long2LongEntrySet() {
    if (entries == null) {
      entries = new Long2LongOpenHashDataMap.MapEntrySet();
    }
    return entries;
  }

  /**
   * An iterator on keys.
   *
   * <p>
   * We simply override the
   * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and
   * possibly their type-specific counterparts) so that they return keys instead of entries.
   */
  private final class KeyIterator extends Long2LongOpenHashDataMap.MapIterator<LongConsumer>
      implements LongIterator {
    public KeyIterator() {
      super();
    }

    // forEachRemaining inherited from MapIterator superclass.
    // Despite the superclass declared with generics, the way Java inherits and generates bridge
    // methods
    // avoids the boxing/unboxing
    @Override
    void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(key.get(index));
    }

    @Override
    public long nextLong() {
      return key.get(nextEntry());
    }
  }

  private final class KeySpliterator
      extends
      Long2LongOpenHashDataMap.MapSpliterator<LongConsumer, Long2LongOpenHashDataMap.KeySpliterator>
      implements LongSpliterator {
    private static final int POST_SPLIT_CHARACTERISTICS =
        LongSpliterators.SET_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

    KeySpliterator() {}

    KeySpliterator(long pos, long max, boolean mustReturnNull, boolean hasSplit) {
      super(pos, max, mustReturnNull, hasSplit);
    }

    @Override
    public int characteristics() {
      return hasSplit ? POST_SPLIT_CHARACTERISTICS
          : LongSpliterators.SET_SPLITERATOR_CHARACTERISTICS;
    }

    @Override
    void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(key.get(index));
    }

    @Override
    Long2LongOpenHashDataMap.KeySpliterator makeForSplit(
        long pos, long max,
        boolean mustReturnNull) {
      return new Long2LongOpenHashDataMap.KeySpliterator(pos, max, mustReturnNull, true);
    }
  }

  private final class KeySet extends AbstractLongSet {
    @Override
    public LongIterator iterator() {
      return new Long2LongOpenHashDataMap.KeyIterator();
    }

    @Override
    public LongSpliterator spliterator() {
      return new Long2LongOpenHashDataMap.KeySpliterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(final java.util.function.LongConsumer consumer) {
      if (containsNullKey) {
        consumer.accept(key.get(n));
      }
      for (long pos = n; pos-- != 0;) {
        final long k = key.get(pos);
        if (!(k == 0)) {
          consumer.accept(k);
        }
      }
    }

    @Override
    public int size() {
      return (int) Math.min(size64(), Integer.MAX_VALUE);
    }

    @Override
    public boolean contains(long k) {
      return containsKey(k);
    }

    @Override
    public boolean remove(long k) {
      final long oldSize = size.get();
      Long2LongOpenHashDataMap.this.remove(k);
      return size.get() != oldSize;
    }

    @Override
    public void clear() {
      Long2LongOpenHashDataMap.this.clear();
    }
  }

  @Override
  public LongSet keySet() {
    if (keys == null) {
      keys = new Long2LongOpenHashDataMap.KeySet();
    }
    return keys;
  }

  /**
   * An iterator on values.
   *
   * <p>
   * We simply override the
   * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and
   * possibly their type-specific counterparts) so that they return values instead of entries.
   */
  private final class ValueIterator extends Long2LongOpenHashDataMap.MapIterator<LongConsumer>
      implements LongIterator {
    public ValueIterator() {
      super();
    }

    // forEachRemaining inherited from MapIterator superclass.
    // Despite the superclass declared with generics, the way Java inherits and generates bridge
    // methods
    // avoids the boxing/unboxing
    @Override
    void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(value.get(index));
    }

    @Override
    public long nextLong() {
      return value.get(nextEntry());
    }
  }

  private final class ValueSpliterator
      extends
      Long2LongOpenHashDataMap.MapSpliterator<LongConsumer, Long2LongOpenHashDataMap.ValueSpliterator>
      implements LongSpliterator {
    private static final int POST_SPLIT_CHARACTERISTICS =
        LongSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

    ValueSpliterator() {}

    ValueSpliterator(long pos, long max, boolean mustReturnNull, boolean hasSplit) {
      super(pos, max, mustReturnNull, hasSplit);
    }

    @Override
    public int characteristics() {
      return hasSplit ? POST_SPLIT_CHARACTERISTICS
          : LongSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS;
    }

    @Override
    void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(value.get(index));
    }

    @Override
    Long2LongOpenHashDataMap.ValueSpliterator makeForSplit(long pos, long max,
        boolean mustReturnNull) {
      return new Long2LongOpenHashDataMap.ValueSpliterator(pos, max, mustReturnNull, true);
    }
  }

  @Override
  public LongCollection values() {
    if (values == null)
      values = new AbstractLongCollection() {
        @Override
        public LongIterator iterator() {
          return new Long2LongOpenHashDataMap.ValueIterator();
        }

        @Override
        public LongSpliterator spliterator() {
          return new Long2LongOpenHashDataMap.ValueSpliterator();
        }

        /** {@inheritDoc} */
        @Override
        public void forEach(final java.util.function.LongConsumer consumer) {
          if (containsNullKey) {
            consumer.accept(value.get(n));
          }
          for (long pos = n; pos-- != 0;) {
            if (!(key.get(pos) == 0)) {
              consumer.accept(value.get(pos));
            }
          }
        }

        @Override
        public int size() {
          return (int) Math.min(size64(), Integer.MAX_VALUE);
        }

        @Override
        public boolean contains(long v) {
          return containsValue(v);
        }

        @Override
        public void clear() {
          Long2LongOpenHashDataMap.this.clear();
        }
      };
    return values;
  }

  /**
   * Rehashes the map, making the table as small as possible.
   *
   * <p>
   * This method rehashes the table to the smallest size satisfying the load factor. It can be used
   * when the set will not be changed anymore, so to optimize access speed and size.
   *
   * <p>
   * If the table size is already the minimum possible, this method does nothing.
   *
   * @return true if there was enough memory to trim the map.
   */
  public boolean trim() {
    return trim(size.get());
  }

  /**
   * Rehashes this map if the table is too large.
   *
   * <p>
   * Let <var>N</var> be the smallest table size that can hold <code>max(n,{@link #size()})</code>
   * entries, still satisfying the load factor. If the current table size is smaller than or equal
   * to <var>N</var>, this method does nothing. Otherwise, it rehashes this map in a table of size
   * <var>N</var>.
   *
   * <p>
   * This method is useful when reusing maps. {@linkplain #clear() Clearing a map} leaves the table
   * size untouched. If you are reusing a map many times, you can call this method with a typical
   * size to avoid keeping around a very large table just because of a few large maps.
   *
   * @param n the threshold for the trimming.
   * @return true if there was enough memory to trim the map.
   * @see #trim()
   */
  public boolean trim(final long n) {
    final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
    if (l >= this.n || size.get() > maxFill(l, f))
      return true;
    try {
      rehash(l);
    } catch (OutOfMemoryError cantDoIt) {
      return false;
    }
    return true;
  }

  /**
   * Rehashes the map.
   *
   * <p>
   * This method implements the basic rehashing strategy, and may be overridden by subclasses
   * implementing different rehashing strategies (e.g., disk-based rehashing). However, you should
   * not override this method unless you understand the internal workings of this class.
   *
   * @param newN the new size
   */
  protected void rehash(final long newN) {
    final long mask = newN - 1; // Note that this is used by the hashing macro
    final AbstractDataList<Long> newKey = keySupplier.get();
    final AbstractDataList<Long> newValue = valueSupplier.get();
    long i = n, pos;
    for (long j = realSize(); j-- != 0;) {
      while ((key.get(--i) == 0));
      if (!(newKey.get(pos = (int) mix(key.get(i)) & mask) == 0)) {
        while (!(newKey.get(pos = (pos + 1) & mask) == 0));
      }
      newKey.set(pos, key.get(i));
      newValue.set(pos, value.get(i));
    }
    newValue.set(n, value.get(n));
    this.n = newN;
    this.mask = mask;
    this.maxFill = maxFill(n, f);
    this.key = newKey;
    this.value = newValue;
  }
}
