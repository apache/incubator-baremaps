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
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An open addressed hash map of long keys and long values derived from fastutil's
 * {@link Long2ObjectOpenHashMap}. This implementation allows for the use of on-heap, off-heap, or
 * memory mapped memory.
 */
public class Long2ObjectOpenHashDataMap<V> extends AbstractLong2ObjectMap<V>
    implements DataMap<V>, Hash {

  private final Supplier<DataMap<Long>> keySupplier;

  private final Supplier<DataMap<V>> valueSupplier;

  /**
   * The array of keys.
   */
  protected DataMap<Long> key;
  /**
   * The array of values.
   */
  protected DataMap<V> value;
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
  protected AtomicLong size = new AtomicLong();;
  /**
   * The acceptable load factor.
   */
  protected final float f;
  /**
   * Cached set of entries.
   */
  protected FastEntrySet<V> entries;
  /**
   * Cached set of keys.
   */
  protected LongSet keys;
  /**
   * Cached collection of values.
   */
  protected ObjectCollection<V> values;

  /**
   * Creates a new hash map.
   *
   * <p>
   * The actual table size will be the least power of two greater than {@code expected}/{@code f}.
   *
   * @param expected the expected number of elements in the hash map.
   * @param f the load factor.
   */
  public Long2ObjectOpenHashDataMap(
      final long expected,
      final float f,
      final Supplier<DataMap<Long>> keySupplier,
      final Supplier<DataMap<V>> valueSupplier) {
    if (f <= 0 || f >= 1) {
      throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
    }
    if (expected < 0) {
      throw new IllegalArgumentException("The expected number of elements must be nonnegative");
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
    final long needed = Math.min(1 << 30,
        Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / f))));
    if (needed > n) {
      rehash(needed);
    }
  }

  private V removeEntry(final long pos) {
    final V oldValue = value.get(pos);
    value.put(pos, null);
    size.decrementAndGet();
    shiftKeys(pos);
    if (n > minN && size.get() < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
      rehash(n / 2);
    }
    return oldValue;
  }

  private V removeNullEntry() {
    containsNullKey = false;
    final V oldValue = value.get(n);
    value.put(n, null);
    size.decrementAndGet();
    if (n > minN && size.get() < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
      rehash(n / 2);
    }
    return oldValue;
  }

  @Override
  public void putAll(Map<? extends Long, ? extends V> m) {
    if (f <= .5) {
      ensureCapacity(m.size()); // The resulting map will be sized for m.size() elements
    } else {
      tryCapacity(size() + m.size()); // The resulting map will be tentatively sized for size() +
    }
    // m.size()
    // elements
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

  private void insert(final long pos, final long k, final V v) {
    if (pos == n) {
      containsNullKey = true;
    }
    key.put(pos, k);
    value.put(pos, v);
    if (size.getAndIncrement() >= maxFill) {
      rehash(bigArraySize(size.get() + 1, f));
    }
  }

  @Override
  public V put(final long k, final V v) {
    final long pos = find(k);
    if (pos < 0) {
      insert(-pos - 1, k, v);
      return defRetValue;
    }
    final V oldValue = value.get(pos);
    value.put(pos, v);
    return oldValue;
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
          key.put(last, 0L);
          value.put(last, null);
          return;
        }
        slot = HashCommon.mix((curr)) & mask;
        if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
          break;
        }
        pos = (pos + 1) & mask;
      }
      key.put(last, curr);
      value.put(last, value.get(pos));
    }
  }

  @Override

  public V remove(final long k) {
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

  public V get(final long k) {
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
  public boolean containsValue(final Object v) {
    if (containsNullKey && java.util.Objects.equals(value.get(n), v)) {
      return true;
    }
    for (long i = n; i-- != 0;) {
      if (!((key.get(i)) == 0) && java.util.Objects.equals(value.get(i), v)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override

  public V getOrDefault(final long k, final V defaultValue) {
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
  public V putIfAbsent(final long k, final V v) {
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

  public boolean remove(final long k, final Object v) {
    if (((k) == 0)) {
      if (containsNullKey && java.util.Objects.equals(v, value.get(n))) {
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
    if (((k) == (curr)) && java.util.Objects.equals(v, value.get(pos))) {
      removeEntry(pos);
      return true;
    }
    while (true) {
      if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
        return false;
      }
      if (((k) == (curr)) && java.util.Objects.equals(v, value.get(pos))) {
        removeEntry(pos);
        return true;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean replace(final long k, final V oldValue, final V v) {
    final long pos = find(k);
    if (pos < 0 || !java.util.Objects.equals(oldValue, value.get(pos))) {
      return false;
    }
    value.put(pos, v);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V replace(final long k, final V v) {
    final long pos = find(k);
    if (pos < 0) {
      return defRetValue;
    }
    final V oldValue = value.get(pos);
    value.put(pos, v);
    return oldValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V computeIfAbsent(final long k,
      final java.util.function.LongFunction<? extends V> mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    final long pos = find(k);
    if (pos >= 0) {
      return value.get(pos);
    }
    final V newValue = mappingFunction.apply(k);
    insert(-pos - 1, k, newValue);
    return newValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V computeIfAbsent(final long key, final Long2ObjectFunction<? extends V> mappingFunction) {
    java.util.Objects.requireNonNull(mappingFunction);
    final long pos = find(key);
    if (pos >= 0) {
      return value.get(pos);
    }
    if (!mappingFunction.containsKey(key)) {
      return defRetValue;
    }
    final V newValue = mappingFunction.get(key);
    insert(-pos - 1, key, newValue);
    return newValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V computeIfPresent(final long k,
      final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    final long pos = find(k);
    if (pos < 0) {
      return defRetValue;
    }
    if (value.get(pos) == null) {
      return defRetValue;
    }
    final V newValue = remappingFunction.apply(k, (value.get(pos)));
    if (newValue == null) {
      if (((k) == 0)) {
        removeNullEntry();
      } else {
        removeEntry(pos);
      }
      return defRetValue;
    }
    value.put(pos, newValue);
    return newValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V compute(final long k,
      final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    final long pos = find(k);
    final V newValue = remappingFunction.apply(k, pos >= 0 ? (value.get(pos)) : null);
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
    if (pos < 0) {
      insert(-pos - 1, k, (newValue));
      return (newValue);
    }
    value.put(pos, (newValue));
    return (newValue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V merge(final long k, final V v,
      final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    java.util.Objects.requireNonNull(remappingFunction);
    java.util.Objects.requireNonNull(v);
    final long pos = find(k);
    if (pos < 0 || value.get(pos) == null) {
      if (pos < 0) {
        insert(-pos - 1, k, v);
      } else {
        value.put(pos, v);
      }
      return v;
    }
    final V newValue = remappingFunction.apply((value.get(pos)), (v));
    if (newValue == null) {
      if (((k) == 0)) {
        removeNullEntry();
      } else {
        removeEntry(pos);
      }
      return defRetValue;
    }
    value.put(pos, newValue);
    return newValue;
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
    // TODO: Arrays.fill(key, 0);
    // TODO: Arrays.fill(value, null);
  }

  @Override
  public long sizeAsLong() {
    return size.get();
  }

  @Override
  public int size() {
    return (int) Math.min(sizeAsLong(), Integer.MAX_VALUE);
  }

  @Override
  public boolean isEmpty() {
    return sizeAsLong() == 0;
  }

  /**
   * The entry class for a hash map does not record key and value, but rather the position in the
   * hash table of the corresponding entry. This is necessary so that calls to
   * {@link java.util.Map.Entry#setValue(Object)} are reflected in the map
   */
  final class MapEntry implements Long2ObjectMap.Entry<V>, Map.Entry<Long, V>, LongObjectPair<V> {
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
    public V getValue() {
      return value.get(index);
    }

    @Override
    public V right() {
      return value.get(index);
    }

    @Override
    public V setValue(final V v) {
      final V oldValue = value.get(index);
      value.put(index, v);
      return oldValue;
    }

    @Override
    public LongObjectPair<V> right(final V v) {
      value.put(index, v);
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
      return Long.valueOf(key.get(index));
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<Long, V> e = (Map.Entry<Long, V>) o;
      return ((key.get(index)) == ((e.getKey()).longValue()))
          && java.util.Objects.equals(value.get(index), (e.getValue()));
    }

    @Override
    public int hashCode() {
      return HashCommon.long2int(key.get(index))
          ^ ((value.get(index)) == null ? 0 : (value.get(index)).hashCode());
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
    boolean mustReturnNullKey = Long2ObjectOpenHashDataMap.this.containsNullKey;
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
          final long k = wrapped.getLong((int) -pos - 1); // TODO: check if -pos - 1 is correct
          long p = HashCommon.mix((k)) & mask;
          while (!((k) == (key.get(p)))) {
            p = (p + 1) & mask;
          }
          return p;
        }
        if (!((key.get(pos)) == 0)) {
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
          long p = HashCommon.mix((k)) & mask;
          while (!((k) == (key.get(p)))) {
            p = (p + 1) & mask;
          }
          acceptOnIndex(action, p);
          c--;
        } else if (!((key.get(pos)) == 0)) {
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
            key.put(last, 0L);
            value.put(last, null);
            return;
          }
          slot = HashCommon.mix((curr)) & mask;
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
        key.put(last, curr);
        value.put(last, value.get(pos));
      }
    }

    public void remove() {
      if (last == -1) {
        throw new IllegalStateException();
      }
      if (last == n) {
        containsNullKey = false;
        value.put(n, null);
      } else if (pos >= 0) {
        shiftKeys(last);
      } else {
        // We're removing wrapped entries.
        Long2ObjectOpenHashDataMap.this.remove(wrapped.getLong((int) -pos - 1));
        last = -1; // Note that we must not decrement size
        return;
      }
      size.decrementAndGet();
      last = -1; // You can no longer remove this entry.
    }

    public long skip(final long n) {
      long i = n;
      while (i-- != 0 && hasNext()) {
        nextEntry();
      }
      return n - i - 1;
    }
  }

  private final class EntryIterator extends MapIterator<Consumer<? super Long2ObjectMap.Entry<V>>>
      implements ObjectIterator<Long2ObjectMap.Entry<V>> {
    private MapEntry entry;

    @Override
    public MapEntry next() {
      return entry = new MapEntry(nextEntry());
    }

    // forEachRemaining inherited from MapIterator superclass.
    @Override
    final void acceptOnIndex(final Consumer<? super Long2ObjectMap.Entry<V>> action,
        final long index) {
      action.accept(entry = new MapEntry(index));
    }

    @Override
    public void remove() {
      super.remove();
      entry.index = -1; // You cannot use a deleted entry.
    }
  }

  private final class FastEntryIterator
      extends MapIterator<Consumer<? super Long2ObjectMap.Entry<V>>>
      implements ObjectIterator<Long2ObjectMap.Entry<V>> {
    private final MapEntry entry = new MapEntry();

    @Override
    public MapEntry next() {
      entry.index = nextEntry();
      return entry;
    }

    // forEachRemaining inherited from MapIterator superclass.
    @Override
    final void acceptOnIndex(final Consumer<? super Long2ObjectMap.Entry<V>> action,
        final long index) {
      entry.index = index;
      action.accept(entry);
    }
  }

  private abstract class MapSpliterator<ConsumerType, SplitType extends MapSpliterator<ConsumerType, SplitType>> {
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
    boolean mustReturnNull = Long2ObjectOpenHashDataMap.this.containsNullKey;
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
        if (!((key.get(pos)) == 0)) {
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
        if (!((key.get(pos)) == 0)) {
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
        if (!((key.get(pos++)) == 0)) {
          ++skipped;
          --n;
        }
      }
      return skipped;
    }
  }

  private final class EntrySpliterator
      extends MapSpliterator<Consumer<? super Long2ObjectMap.Entry<V>>, EntrySpliterator>
      implements ObjectSpliterator<Long2ObjectMap.Entry<V>> {
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
    final void acceptOnIndex(final Consumer<? super Long2ObjectMap.Entry<V>> action,
        final long index) {
      action.accept(new MapEntry(index));
    }

    @Override
    final EntrySpliterator makeForSplit(long pos, long max, boolean mustReturnNull) {
      return new EntrySpliterator(pos, max, mustReturnNull, true);
    }
  }

  private final class MapEntrySet extends AbstractObjectSet<Long2ObjectMap.Entry<V>>
      implements FastEntrySet<V> {
    @Override
    public ObjectIterator<Long2ObjectMap.Entry<V>> iterator() {
      return new EntryIterator();
    }

    @Override
    public ObjectIterator<Long2ObjectMap.Entry<V>> fastIterator() {
      return new FastEntryIterator();
    }

    @Override
    public ObjectSpliterator<Long2ObjectMap.Entry<V>> spliterator() {
      return new EntrySpliterator();
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
      final long k = (Long) e.getKey();
      final V v = ((V) e.getValue());
      if (((k) == 0)) {
        return Long2ObjectOpenHashDataMap.this.containsNullKey
            && java.util.Objects.equals(value.get(n), v);
      }
      long curr;
      long pos;
      // The starting point.
      if (((curr = key.get(pos = HashCommon.mix((k)) & mask)) == 0)) {
        return false;
      }
      if (((k) == (curr))) {
        return java.util.Objects.equals(value.get(pos), v);
      }
      // There's always an unused entry.
      while (true) {
        if (((curr = key.get(pos = (pos + 1) & mask)) == 0)) {
          return false;
        }
        if (((k) == (curr))) {
          return java.util.Objects.equals(value.get(pos), v);
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
      final long k = (Long) e.getKey();
      final V v = ((V) e.getValue());
      if (((k) == 0)) {
        if (containsNullKey && java.util.Objects.equals(value.get(n), v)) {
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
      if (((curr) == (k))) {
        if (java.util.Objects.equals(value.get(pos), v)) {
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
          if (java.util.Objects.equals(value.get(pos), v)) {
            removeEntry(pos);
            return true;
          }
        }
      }
    }

    @Override
    public int size() {
      return (int) Math.min(size.get(), Integer.MAX_VALUE);
    }

    @Override
    public void clear() {
      Long2ObjectOpenHashDataMap.this.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(final Consumer<? super Long2ObjectMap.Entry<V>> consumer) {
      if (containsNullKey) {
        consumer.accept(new AbstractLong2ObjectMap.BasicEntry<V>(key.get(n), value.get(n)));
      }
      for (long pos = n; pos-- != 0;) {
        if (!((key.get(pos)) == 0)) {
          consumer.accept(new AbstractLong2ObjectMap.BasicEntry<V>(key.get(pos), value.get(pos)));
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fastForEach(final Consumer<? super Long2ObjectMap.Entry<V>> consumer) {
      if (containsNullKey) {
        consumer.accept(new AbstractLong2ObjectMap.BasicEntry<>(key.get(n), value.get(n)));
      }
      for (long pos = n; pos-- != 0;) {
        if (!((key.get(pos)) == 0)) {
          consumer.accept(new AbstractLong2ObjectMap.BasicEntry<>(key.get(pos), value.get(pos)));
        }
      }
    }
  }

  @Override
  public FastEntrySet<V> long2ObjectEntrySet() {
    if (entries == null) {
      entries = new MapEntrySet();
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
  private final class KeyIterator extends MapIterator<java.util.function.LongConsumer>
      implements LongIterator {
    public KeyIterator() {
      super();
    }

    // forEachRemaining inherited from MapIterator superclass.
    // Despite the superclass declared with generics, the way Java inherits and generates bridge
    // methods
    // avoids the boxing/unboxing
    @Override
    final void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(key.get(index));
    }

    @Override
    public long nextLong() {
      return key.get(nextEntry());
    }
  }

  private final class KeySpliterator extends
      MapSpliterator<java.util.function.LongConsumer, KeySpliterator> implements LongSpliterator {
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
    final void acceptOnIndex(final java.util.function.LongConsumer action, final long index) {
      action.accept(key.get(index));
    }

    @Override
    final KeySpliterator makeForSplit(long pos, long max, boolean mustReturnNull) {
      return new KeySpliterator(pos, max, mustReturnNull, true);
    }
  }

  private final class KeySet extends AbstractLongSet {
    @Override
    public LongIterator iterator() {
      return new KeyIterator();
    }

    @Override
    public LongSpliterator spliterator() {
      return new KeySpliterator();
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
        if (!((k) == 0)) {
          consumer.accept(k);
        }
      }
    }

    @Override
    public int size() {
      return (int) Math.min(size.get(), Integer.MAX_VALUE);
    }

    @Override
    public boolean contains(long k) {
      return containsKey(k);
    }

    @Override
    public boolean remove(long k) {
      final long oldSize = size.get();
      Long2ObjectOpenHashDataMap.this.remove(k);
      return size.get() != oldSize;
    }

    @Override
    public void clear() {
      Long2ObjectOpenHashDataMap.this.clear();
    }
  }

  @Override
  public LongSet keySet() {
    if (keys == null) {
      keys = new KeySet();
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
  private final class ValueIterator extends MapIterator<Consumer<? super V>>
      implements ObjectIterator<V> {
    public ValueIterator() {
      super();
    }

    // forEachRemaining inherited from MapIterator superclass.
    // Despite the superclass declared with generics, the way Java inherits and generates bridge
    // methods
    // avoids the boxing/unboxing
    @Override
    final void acceptOnIndex(final Consumer<? super V> action, final long index) {
      action.accept(value.get(index));
    }

    @Override
    public V next() {
      return value.get(nextEntry());
    }
  }

  private final class ValueSpliterator extends MapSpliterator<Consumer<? super V>, ValueSpliterator>
      implements ObjectSpliterator<V> {
    private static final int POST_SPLIT_CHARACTERISTICS =
        ObjectSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

    ValueSpliterator() {}

    ValueSpliterator(long pos, long max, boolean mustReturnNull, boolean hasSplit) {
      super(pos, max, mustReturnNull, hasSplit);
    }

    @Override
    public int characteristics() {
      return hasSplit ? POST_SPLIT_CHARACTERISTICS
          : ObjectSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS;
    }

    @Override
    final void acceptOnIndex(final Consumer<? super V> action, final long index) {
      action.accept(value.get(index));
    }

    @Override
    final ValueSpliterator makeForSplit(long pos, long max, boolean mustReturnNull) {
      return new ValueSpliterator(pos, max, mustReturnNull, true);
    }
  }

  @Override
  public ObjectCollection<V> values() {
    if (values == null) {
      values = new AbstractObjectCollection<V>() {
        @Override
        public ObjectIterator<V> iterator() {
          return new ValueIterator();
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
          return new ValueSpliterator();
        }

        /** {@inheritDoc} */
        @Override
        public void forEach(final Consumer<? super V> consumer) {
          if (containsNullKey) {
            consumer.accept(value.get(n));
          }
          for (long pos = n; pos-- != 0;) {
            if (!((key.get(pos)) == 0)) {
              consumer.accept(value.get(pos));
            }
          }
        }

        @Override
        public int size() {
          return (int) Math.min(size.get(), Integer.MAX_VALUE);
        }

        @Override
        public boolean contains(Object v) {
          return containsValue(v);
        }

        @Override
        public void clear() {
          Long2ObjectOpenHashDataMap.this.clear();
        }
      };
    }
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
    final long l = HashCommon.nextPowerOfTwo((long) Math.ceil(n / f));
    if (l >= this.n || size.get() > maxFill(l, f)) {
      return true;
    }
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
    final DataMap<Long> newKey = keySupplier.get();
    final DataMap<V> newValue = valueSupplier.get();
    long i = n, pos;
    for (long j = realSize(); j-- != 0;) {
      while ((key.get(--i) == 0));
      if (!((newKey.get(pos = mix((key.get(i))) & mask)) == 0)) {
        while (!(newKey.get(pos = (pos + 1) & mask) == 0));
      }
      newKey.put(pos, key.get(i));
      newValue.put(pos, value.get(i));
    }
    newValue.put(newN, value.get(n));
    this.n = newN;
    this.mask = mask;
    this.maxFill = maxFill(n, f);
    this.key = newKey;
    this.value = newValue;
  }
}
