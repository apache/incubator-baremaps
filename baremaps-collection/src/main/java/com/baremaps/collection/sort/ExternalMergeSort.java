/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.collection.sort;

import com.baremaps.collection.DataList;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.LongDataType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExternalMergeSort<T> {

  public final int maxTempFiles = 1024;

  private final Supplier<DataList<T>> tempListSupplier;

  public ExternalMergeSort(Supplier<DataList<T>> tempListSupplier) {
    this.tempListSupplier = tempListSupplier;
  }

  public long estimateAvailableMemory() {
    System.gc();
    // http://stackoverflow.com/questions/12807797/java-get-available-memory
    Runtime r = Runtime.getRuntime();
    long allocatedMemory = r.totalMemory() - r.freeMemory();
    long presFreeMemory = r.maxMemory() - allocatedMemory;
    return presFreeMemory;
  }

  public long estimateBestSizeOfBlocks(
      final long sizeOfFile, final long maxMemory) {
    // we don't want to open up much more than maxtmpfiles temporary
    // files, better run
    // out of memory first.
    long blockSize = sizeOfFile / maxTempFiles + (sizeOfFile % maxTempFiles == 0 ? 0 : 1);

    // on the other hand, we don't want to create many temporary
    // files
    // for naught. If blocksize is smaller than half the free
    // memory, grow it.
    if (blockSize < maxMemory / 2) {
      blockSize = maxMemory / 2;
    }
    return blockSize;
  }

  protected long mergeSortedFiles(
      List<DataList<T>> inputs, DataList<T> output, final Comparator<T> comparator, boolean distinct)
      throws IOException {
    PriorityQueue<DataStack<T>> queue =
        new PriorityQueue<>(
            inputs.size(),
            (i, j) -> comparator.compare(i.peek(), j.peek()));
    for (DataList<T> input : inputs) {
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

    for (DataList<T> dataList : inputs) {
      dataList.clean();
    }

    return counter;
  }

  public void sort(DataList<T> input, DataList<T> output, final Comparator<T> comparator)
      throws IOException {
    mergeSortedFiles(sortInBatch(input, comparator, maxTempFiles,
        estimateAvailableMemory(), null, false,
        false), output, comparator, false);
  }

  protected DataList<T> sortBatch(
      List<T> batch,
      Comparator<T> comparator,
      File tempDirectory,
      boolean distinct,
      boolean parallel) throws IOException {
    DataList<T> output = tempListSupplier.get();
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

  protected List<DataList<T>> sortInBatch(
      final DataList<T> input,
      final Comparator<T> comparator,
      final int maxTempFiles,
      long maxMemory,
      final File tempDirectory,
      final boolean distinct,
      final boolean parallel) throws IOException {
    List<DataList<T>> lists = new ArrayList<>();
    long blockSize = estimateBestSizeOfBlocks(input.size(), maxMemory);
    List<T> block = new ArrayList<>();
    long inputIndex = 0;
    while (inputIndex < input.size()) {
      long blockIndex = 0;
      while (blockIndex < blockSize && inputIndex < input.size()) {
        block.add(input.get(inputIndex));
        inputIndex++;
        blockIndex++;
      }
      lists.add(sortBatch(block, comparator, tempDirectory, distinct, parallel));
      block.clear();
    }
    return lists;
  }

}
