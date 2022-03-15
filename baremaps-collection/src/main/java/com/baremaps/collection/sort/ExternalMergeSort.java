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

import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.memory.OnDiskFileMemory;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.LongDataType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Goal: offer a generic external-memory sorting program in Java.
 *
 * <p>It must be : - hackable (easy to adapt) - scalable to large files - sensibly efficient.
 *
 * <p>This software is in the public domain.
 *
 * <p>Usage: java com/google/code/externalsorting/ExternalSort somefile.txt out.txt
 *
 * <p>You can change the default maximal number of temporary files with the -t flag: java
 * com/google/code/externalsorting/ExternalSort somefile.txt out.txt -t 3
 *
 * <p>For very large files, you might want to use an appropriate flag to allocate more memory to the
 * Java VM: java -Xms2G com/google/code/externalsorting/ExternalSort somefile.txt out.txt
 *
 * <p>By (in alphabetical order) Philippe Beaudoin, Eleftherios Chetzakis, Jon Elsas, Christan
 * Grant, Daniel Haran, Daniel Lemire, Sugumaran Harikrishnan, Amit Jain, Thomas Mueller, Jerry Yang, First published:
 * April 2010 originally posted at http://lemire.me/blog/archives/2010/04/01/external-memory-sorting-in-java/
 */
public class ExternalMergeSort {

  /**
   * Default maximal number of temporary files allowed.
   */
  public static final int DEFAULTMAXTEMPFILES = 1024;

  /**
   * This method calls the garbage collector and then returns the free memory. This avoids problems with applications
   * where the GC hasn't reclaimed memory and reports no available memory.
   *
   * @return available memory
   */
  public static long estimateAvailableMemory() {
    System.gc();
    // http://stackoverflow.com/questions/12807797/java-get-available-memory
    Runtime r = Runtime.getRuntime();
    long allocatedMemory = r.totalMemory() - r.freeMemory();
    long presFreeMemory = r.maxMemory() - allocatedMemory;
    return presFreeMemory;
  }

  /**
   * we divide the file into small blocks. If the blocks are too small, we shall create too many temporary files. If
   * they are too big, we shall be using too much memory.
   *
   * @param sizeoffile  how much data (in bytes) can we expect
   * @param maxtmpfiles how many temporary files can we create (e.g., 1024)
   * @param maxMemory   Maximum memory to use (in bytes)
   * @return the estimate
   */
  public static long estimateBestSizeOfBlocks(
      final long sizeoffile, final int maxtmpfiles, final long maxMemory) {
    // we don't want to open up much more than maxtmpfiles temporary
    // files, better run
    // out of memory first.
    long blocksize = sizeoffile / maxtmpfiles + (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

    // on the other hand, we don't want to create many temporary
    // files
    // for naught. If blocksize is smaller than half the free
    // memory, grow it.
    if (blocksize < maxMemory / 2) {
      blocksize = maxMemory / 2;
    }
    return blocksize;
  }

  /**
   * This merges a bunch of temporary flat files
   *
   * @param files    The {@link List} of sorted {@link File}s to be merged.
   * @param fbw      The output {@link BufferedWriter} to merge the results to.
   * @param cmp      The {@link Comparator} to use to compare {@link String}s.
   * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
   * @return The number of lines sorted.
   * @throws IOException generic IO exception
   * @since v0.1.4
   */
  public static long mergeSortedFiles(
      List<AlignedDataList<Long>> files, AlignedDataList<Long> fbw, final Comparator<Long> cmp, boolean distinct) {


    PriorityQueue<AlignedDataStack> pq =
        new PriorityQueue<>(
            11,
            (i, j) -> cmp.compare(i.peek(), j.peek()));

    for (AlignedDataList<Long> f : files) {
      if (f.size() == 0) {
        continue;
      }
      AlignedDataStack bfb = new AlignedDataStack(f);
      if (!bfb.empty()) {
        pq.add(bfb);
      }
    }


    long rowcounter = 0;
    if (!distinct) {
      while (pq.size() > 0) {
        AlignedDataStack bfb = pq.poll();
        Long r = bfb.pop();
        fbw.add(r);
        ++rowcounter;
        if (bfb.empty()) {
          bfb.close();
        } else {
          pq.add(bfb); // add it back
        }
      }
    } else {
      Long lastLine = null;
      if (pq.size() > 0) {
        AlignedDataStack bfb = pq.poll();
        lastLine = bfb.pop();
        fbw.add(lastLine);
        ++rowcounter;
        if (bfb.empty()) {
          bfb.close();
        } else {
          pq.add(bfb); // add it back
        }
      }
      while (pq.size() > 0) {
        AlignedDataStack bfb = pq.poll();
        Long r = bfb.pop();
        // Skip duplicate lines
        if (cmp.compare(r, lastLine) != 0) {
          fbw.add(r);
          lastLine = r;
        }
        ++rowcounter;
        if (bfb.empty()) {
          bfb.close();
        } else {
          pq.add(bfb); // add it back
        }
      }
    }

    // TODO: delete the tmp files

    return rowcounter;
  }

  /**
   * This sorts a file (input) to an output file (output) using customized comparator
   *
   * @param input  source file
   * @param output output file
   * @param cmp    The {@link Comparator} to use to compare {@link String}s.
   * @throws IOException generic IO exception
   */
  public static void sort(AlignedDataList<Long> input, AlignedDataList<Long> output, final Comparator<Long> cmp)
      throws IOException {
    List<AlignedDataList<Long>> blocks = ExternalMergeSort.sortInBatch(input, cmp, DEFAULTMAXTEMPFILES,
        1000, null, false,
        true);
    ExternalMergeSort.mergeSortedFiles(blocks, output, cmp, false);
  }

  /**
   * Sort a list and save it to a temporary file
   *
   * @param tmplist      data to be sorted
   * @param cmp          string comparator
   * @param tmpdirectory location of the temporary files (set to null for default location)
   * @param distinct     Pass <code>true</code> if duplicate lines should be discarded.
   * @param parallel     set to <code>true</code> when sorting in parallel
   * @return the file containing the sorted data
   * @throws IOException generic IO exception
   */
  public static AlignedDataList<Long> sortAndSave(
      List<Long> tmplist,
      Comparator<Long> cmp,
      File tmpdirectory,
      boolean distinct,
      boolean parallel) throws IOException {

    // TODO: store in temporary file
    AlignedDataList<Long> out = new AlignedDataList<Long>(new LongDataType(), new OnHeapMemory());

    Stream<Long> tmpStream = tmplist.stream().sorted(cmp);
    if (parallel) {
      tmpStream = tmpStream.parallel();
    }
    if (distinct) {
      tmpStream = tmpStream.distinct();
    }

    tmpStream.forEachOrdered(out::add);

    return out;
  }

  /**
   * This will simply load the file by blocks of lines, then sort them in-memory, and write the result to temporary
   * files that have to be merged later.
   *
   * @param input
   * @param cmp          string comparator
   * @param maxtmpfiles  maximal number of temporary files
   * @param maxMemory    maximum amount of memory to use (in bytes)
   * @param tmpdirectory location of the temporary files (set to null for default location)
   * @param distinct     Pass <code>true</code> if duplicate lines should be discarded.
   * @param parallel     sort in parallel
   * @return a list of temporary flat files
   * @throws IOException generic IO exception
   */
  public static List<AlignedDataList<Long>> sortInBatch(
      final AlignedDataList<Long> input,
      final Comparator<Long> cmp,
      final int maxtmpfiles,
      long maxMemory,
      final File tmpdirectory,
      final boolean distinct,
      final boolean parallel) throws IOException {
    List<AlignedDataList<Long>> lists = new ArrayList<>();
    long blockSize = estimateBestSizeOfBlocks(input.size(), maxtmpfiles, maxMemory);
    List<Long> block = new ArrayList<>();
    long inputIndex = 0;
    while (inputIndex < input.size()) {
      long blockIndex = 0;
      while (blockIndex < blockSize && inputIndex < input.size()) {
        block.add(input.get(inputIndex));
        inputIndex++;
        blockIndex++;
      }
      lists.add(sortAndSave(block, cmp, tmpdirectory, distinct, parallel));
      block.clear();
    }
    return lists;
  }


  public static void main(String... args) throws IOException {
    AlignedDataList<Long> input = new AlignedDataList<>(new LongDataType(), new OnHeapMemory());
    Random random = new Random(0);
    for (long i = 0; i < 10000; i++) {
      input.add(random.nextLong());
    }
    AlignedDataList<Long> output = new AlignedDataList<>(new LongDataType(), new OnHeapMemory());
    sort(input, output, Long::compareTo);
    for (long i = 0; i < output.size(); i++) {
      System.out.println(output.get(i));
    }

    System.out.println(output.size());
  }

}
