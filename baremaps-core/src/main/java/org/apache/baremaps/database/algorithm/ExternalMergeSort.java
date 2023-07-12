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

package org.apache.baremaps.database.algorithm;



import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.baremaps.database.collection.AbstractDataList;

/**
 * An external merge sort algorithm adapted from
 * <a href="https://github.com/lemire/externalsortinginjava">externalsortinginjava</a> (public
 * domain).
 */
public class ExternalMergeSort {

  /**
   * Sorts an input list to an output list.
   *
   * @param <T> The type of the list
   * @param input The input list to sort
   * @param output The output list
   * @param comparator The comparator that tells how to sort the lines
   * @param tempLists The supplier of temporary lists
   * @param batchSize The batch size
   * @param distinct The flag indicating if duplicates should be discarded
   * @param parallel The flag indicating if parallelism should be used
   * @throws IOException
   */
  public static <T> void sort(
      AbstractDataList<T> input,
      AbstractDataList<T> output,
      final Comparator<T> comparator,
      Supplier<AbstractDataList<T>> tempLists,
      long batchSize,
      boolean distinct,
      boolean parallel) throws IOException {
    mergeSortedBatches(sortInBatch(input, comparator, tempLists, batchSize, distinct, parallel),
        output, comparator, distinct);
  }

  /**
   * Merges several batches to an output list.
   *
   * @param <T> The type of the list
   * @param batches The input batches to merge
   * @param output The output list
   * @param comparator The comparator that tells how to sort the lines
   * @param distinct The flag indicating if duplicates should be discarded
   * @return the number of data sorted
   * @throws IOException
   */
  private static <T> long mergeSortedBatches(
      List<AbstractDataList<T>> batches,
      AbstractDataList<T> output,
      Comparator<T> comparator,
      boolean distinct) throws IOException {

    PriorityQueue<DataStack<T>> queue =
        new PriorityQueue<>(batches.size(), (i, j) -> comparator.compare(i.peek(), j.peek()));

    for (AbstractDataList<T> input : batches) {
      if (input.size64() == 0) {
        continue;
      }
      DataStack<T> stack = new DataStack<>(input);
      if (!stack.empty()) {
        queue.add(stack);
      }
    }

    long counter = 0;
    if (!distinct) {
      while (queue.size() > 0) {
        DataStack<T> stack = queue.poll();
        T value = stack.pop();
        output.addIndexed(value);
        ++counter;
        if (stack.empty()) {
          stack.close();
        } else {
          queue.add(stack); // add it back
        }
      }
    } else {
      T last = null;
      if (queue.size() > 0) {
        DataStack<T> stack = queue.poll();
        last = stack.pop();
        output.addIndexed(last);
        ++counter;
        if (stack.empty()) {
          stack.close();
        } else {
          queue.add(stack);
        }
      }
      while (queue.size() > 0) {
        DataStack<T> stack = queue.poll();
        T value = stack.pop();
        // Skip duplicate lines
        if (comparator.compare(value, last) != 0) {
          output.addIndexed(value);
          last = value;
        }
        ++counter;
        if (stack.empty()) {
          stack.close();
        } else {
          queue.add(stack); // add it back
        }
      }
    }

    for (AbstractDataList<T> batch : batches) {
      batch.clear();
    }

    return counter;
  }

  /**
   * Sorts a list in several batches that fit in memory.
   *
   * @param input The input list to sort
   * @param comparator The comparator that tells how to sort the lines
   * @param supplier The supplier that creates temporary lists
   * @param batchSize The batch size
   * @param distinct The flag indicating if duplicates should be discarded
   * @param parallel The flag indicating if parallelism should be used
   * @param <T>
   * @return the sorted batches
   */
  public static <T> List<AbstractDataList<T>> sortInBatch(
      final AbstractDataList<T> input,
      final Comparator<T> comparator,
      Supplier<AbstractDataList<T>> supplier,
      long batchSize,
      final boolean distinct,
      final boolean parallel) {
    List<AbstractDataList<T>> batches = new ArrayList<>();
    List<T> batch = new ArrayList<>();

    var iterator = input.iterator();
    while (iterator.hasNext()) {
      var element = iterator.next();
      batch.add(element);
      if (batch.size() >= batchSize || !iterator.hasNext()) {
        var sortedBatch = sortBatch(batch, comparator, supplier, distinct, parallel);
        batches.add(sortedBatch);
        batch.clear();
      }
    }

    return batches;
  }

  /**
   * Sorts a batch.
   *
   * @param batch The batch to sort
   * @param comparator The comparator that tells how to sort the lines
   * @param supplier The supplier that creates temporary lists
   * @param distinct The flag indicating if duplicates should be discarded
   * @param parallel The flag indicating if parallelism should be used
   * @param <T>
   * @return the sorted batch
   */
  public static <T> AbstractDataList<T> sortBatch(
      List<T> batch,
      Comparator<T> comparator,
      Supplier<AbstractDataList<T>> supplier,
      boolean distinct,
      boolean parallel) {
    AbstractDataList<T> output = supplier.get();
    Stream<T> tmpStream = batch.stream().sorted(comparator);
    if (parallel) {
      tmpStream = tmpStream.parallel();
    }
    if (distinct) {
      tmpStream = tmpStream.distinct();
    }
    tmpStream.forEachOrdered(output::addIndexed);
    return output;
  }

  /**
   * A wrapper on top of a {@link AbstractDataList} which keeps the last data record in memory.
   *
   * @param <T>
   */
  static final class DataStack<T> implements AutoCloseable {

    private AbstractDataList<T> list;

    private Long index = 0l;

    private T cache;

    public DataStack(AbstractDataList<T> list) {
      this.list = list;
      reload();
    }

    public boolean empty() {
      return this.index > list.size64();
    }

    public T peek() {
      return this.cache;
    }

    public T pop() {
      T answer = peek(); // make a copy
      reload();
      return answer;
    }

    private void reload() {
      this.cache = this.list.get(index);
      index++;
    }

    @Override
    public void close() {
      this.list.clear();
    }
  }
}
