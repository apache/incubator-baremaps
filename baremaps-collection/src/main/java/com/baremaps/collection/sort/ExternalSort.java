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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

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
public class ExternalSort {

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
   * This merges several BinaryFileBuffer to an output writer.
   *
   * @param fbw      A buffer where we write the data.
   * @param cmp      A comparator object that tells us how to sort the lines.
   * @param distinct Pass <code>true</code> if duplicate lines should be discarded.
   * @param buffers  Where the data should be read.
   * @return The number of lines sorted.
   * @throws IOException generic IO exception
   */
  public static long mergeSortedFiles(
      BufferedWriter fbw,
      final Comparator<String> cmp,
      boolean distinct,
      List<IOStringStack> buffers)
      throws IOException {
    PriorityQueue<IOStringStack> pq =
        new PriorityQueue<>(
            11,
            (i, j) -> cmp.compare(i.peek(), j.peek()));
    for (IOStringStack bfb : buffers) {
      if (!bfb.empty()) {
        pq.add(bfb);
      }
    }
    long rowcounter = 0;
    try {
      if (!distinct) {
        while (pq.size() > 0) {
          IOStringStack bfb = pq.poll();
          String r = bfb.pop();
          fbw.write(r);
          fbw.newLine();
          ++rowcounter;
          if (bfb.empty()) {
            bfb.close();
          } else {
            pq.add(bfb); // add it back
          }
        }
      } else {
        String lastLine = null;
        if (pq.size() > 0) {
          IOStringStack bfb = pq.poll();
          lastLine = bfb.pop();
          fbw.write(lastLine);
          fbw.newLine();
          ++rowcounter;
          if (bfb.empty()) {
            bfb.close();
          } else {
            pq.add(bfb); // add it back
          }
        }
        while (pq.size() > 0) {
          IOStringStack bfb = pq.poll();
          String r = bfb.pop();
          // Skip duplicate lines
          if (cmp.compare(r, lastLine) != 0) {
            fbw.write(r);
            fbw.newLine();
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
    } finally {
      fbw.close();
      for (IOStringStack bfb : pq) {
        bfb.close();
      }
    }
    return rowcounter;
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
      List<File> files, BufferedWriter fbw, final Comparator<String> cmp, boolean distinct)
      throws IOException {
    ArrayList<IOStringStack> bfbs = new ArrayList<>();
    for (File f : files) {
      if (f.length() == 0) {
        continue;
      }
      InputStream in = new FileInputStream(f);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      BinaryFileBuffer bfb = new BinaryFileBuffer(br);
      bfbs.add(bfb);
    }
    long rowcounter = mergeSortedFiles(fbw, cmp, distinct, bfbs);
    for (File f : files) {
      f.delete();
    }
    return rowcounter;
  }

  /**
   * This sorts a file (input) to an output file (output) using customized comparator
   *
   * @param file  source file
   * @param output output file
   * @param cmp    The {@link Comparator} to use to compare {@link String}s.
   * @throws IOException generic IO exception
   */
  public static void sort(final File file, final File output, final Comparator<String> cmp)
      throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

    BufferedWriter fbw =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
    ExternalSort.mergeSortedFiles(
        ExternalSort.sortInBatch(input, file.length(), cmp, DEFAULTMAXTEMPFILES, estimateAvailableMemory(), null, false,
            true), fbw, cmp, false);
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
  public static File sortAndSave(
      List<String> tmplist,
      Comparator<String> cmp,
      File tmpdirectory,
      boolean distinct,
      boolean parallel)
      throws IOException {
    if (parallel) {
      tmplist =
          tmplist.parallelStream()
              .sorted(cmp)
              .collect(Collectors.toCollection(ArrayList<String>::new));
    } else {
      Collections.sort(tmplist, cmp);
    }
    File newtmpfile = File.createTempFile("sortInBatch", "flatfile", tmpdirectory);
    newtmpfile.deleteOnExit();
    OutputStream out = new FileOutputStream(newtmpfile);
    try (BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out))) {
      if (!distinct) {
        for (String r : tmplist) {
          fbw.write(r);
          fbw.newLine();
        }
      } else {
        String lastLine = null;
        Iterator<String> i = tmplist.iterator();
        if (i.hasNext()) {
          lastLine = i.next();
          fbw.write(lastLine);
          fbw.newLine();
        }
        while (i.hasNext()) {
          String r = i.next();
          // Skip duplicate lines
          if (cmp.compare(r, lastLine) != 0) {
            fbw.write(r);
            fbw.newLine();
            lastLine = r;
          }
        }
      }
    }
    return newtmpfile;
  }

  /**
   * This will simply load the file by blocks of lines, then sort them in-memory, and write the result to temporary
   * files that have to be merged later.
   *
   * @param fbr          data source
   * @param datalength   estimated data volume (in bytes)
   * @param cmp          string comparator
   * @param maxtmpfiles  maximal number of temporary files
   * @param maxMemory    maximum amount of memory to use (in bytes)
   * @param tmpdirectory location of the temporary files (set to null for default location)
   * @param distinct     Pass <code>true</code> if duplicate lines should be discarded.
   * @param parallel     sort in parallel
   * @return a list of temporary flat files
   * @throws IOException generic IO exception
   */
  public static List<File> sortInBatch(
      final BufferedReader fbr,
      final long datalength,
      final Comparator<String> cmp,
      final int maxtmpfiles,
      long maxMemory,
      final File tmpdirectory,
      final boolean distinct,
      final boolean parallel)
      throws IOException {
    List<File> files = new ArrayList<>();
    long blocksize = estimateBestSizeOfBlocks(datalength, maxtmpfiles, maxMemory); // in
    // bytes

    try {
      List<String> tmplist = new ArrayList<>();
      String line = "";
      try {
        while (line != null) {
          long currentblocksize = 0; // in bytes
          while ((currentblocksize < blocksize) && ((line = fbr.readLine()) != null)) {
            tmplist.add(line);
            currentblocksize += StringSizeEstimator.estimatedSizeOf(line);
          }
          files.add(sortAndSave(tmplist, cmp, tmpdirectory, distinct, parallel));
          tmplist.clear();
        }
      } catch (EOFException oef) {
        if (tmplist.size() > 0) {
          files.add(sortAndSave(tmplist, cmp, tmpdirectory, distinct, parallel));
          tmplist.clear();
        }
      }
    } finally {
      fbr.close();
    }
    return files;
  }

  /**
   * default comparator between strings.
   */
  public static Comparator<String> defaultcomparator = String::compareTo;

  /**
   * Default maximal number of temporary files allowed.
   */
  public static final int DEFAULTMAXTEMPFILES = 1024;
}
