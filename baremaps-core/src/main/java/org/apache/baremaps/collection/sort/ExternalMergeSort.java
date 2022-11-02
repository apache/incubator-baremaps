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

package org.apache.baremaps.collection.sort;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.baremaps.collection.DataList;

/**
 * External merge sort algorithm adapted
 *
 * <p>
 * This code has been adapted from
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
  public static <T> void sort(DataList<T> input, DataList<T> output, final Comparator<T> comparator,
      Supplier<DataList<T>> tempLists, long batchSize, boolean distinct, boolean parallel)
      throws IOException {
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
  private static <T> long mergeSortedBatches(List<DataList<T>> batches, DataList<T> output,
      Comparator<T> comparator, boolean distinct) throws IOException {
    PriorityQueue<DataStack<T>> queue =
        new PriorityQueue<>(batches.size(), (i, j) -> comparator.compare(i.peek(), j.peek()));
    for (DataList<T> input : batches) {
      if (input.size() == 0) {
        continue;
      }
      DataStack stack = new DataStack(input);
      if (!stack.empty()) {
        queue.add(stack);
      }
    }

    long counter = 0;
    if (!distinct) {
      while (queue.size() > 0) {
        DataStack<T> stack = queue.poll();
        T value = stack.pop();
        output.add(value);
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
        output.add(last);
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
          output.add(value);
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

    for (DataList<T> dataList : batches) {
      dataList.clean();
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
   * @throws IOException
   */
  public static <T> List<DataList<T>> sortInBatch(final DataList<T> input,
      final Comparator<T> comparator, Supplier<DataList<T>> supplier, long batchSize,
      final boolean distinct, final boolean parallel) throws IOException {
    List<DataList<T>> lists = new ArrayList<>();
    List<T> batch = new ArrayList<>();
    long inputIndex = 0;
    while (inputIndex < input.size()) {
      long batchIndex = 0;
      while (batchIndex < batchSize && inputIndex < input.size()) {
        batch.add(input.get(inputIndex));
        inputIndex++;
        batchIndex++;
      }
      lists.add(sortBatch(batch, comparator, supplier, distinct, parallel));
      batch.clear();
    }
    return lists;
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
   * @throws IOException
   */
  public static <T> DataList<T> sortBatch(List<T> batch, Comparator<T> comparator,
      Supplier<DataList<T>> supplier, boolean distinct, boolean parallel) throws IOException {
    DataList<T> output = supplier.get();
    Stream<T> tmpStream = batch.stream().sorted(comparator);
    if (parallel) {
      tmpStream = tmpStream.parallel();
    }
    if (distinct) {
      tmpStream = tmpStream.distinct();
    }
    tmpStream.forEachOrdered(output::add);
    return output;
  }
}
