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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** Unit test for simple App. */
@SuppressWarnings({"static-method", "javadoc"})
public class ExternalSortTest {
  private static final String TEST_FILE1_TXT = "test-file-1.txt";
  private static final String TEST_FILE2_TXT = "test-file-2.txt";
  private static final String TEST_FILE1_CSV = "test-file-1.csv";
  private static final String[] EXPECTED_SORT_RESULTS = {
    "a", "b", "b", "e", "f", "i", "m", "o", "u", "u", "x", "y", "z"
  };
  private static final String[] EXPECTED_MERGE_RESULTS = {
    "a", "a", "b", "c", "c", "d", "e", "e", "f", "g", "g", "h", "i", "j", "k"
  };
  private static final String[] EXPECTED_MERGE_DISTINCT_RESULTS = {
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"
  };
  private static final String[] EXPECTED_HEADER_RESULTS = {
    "HEADER, HEADER", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"
  };
  private static final String[] EXPECTED_DISTINCT_RESULTS = {
    "a", "b", "e", "f", "i", "m", "o", "u", "x", "y", "z"
  };
  private static final String[] SAMPLE = {
    "f", "m", "b", "e", "i", "o", "u", "x", "a", "y", "z", "b", "u"
  };

  private File file1;
  private File file2;
  private File csvFile;
  private List<File> fileList;

  /** @throws Exception */
  @Before
  public void setUp() throws Exception {
    this.fileList = new ArrayList<File>(3);
    this.file1 = new File(this.getClass().getClassLoader().getResource(TEST_FILE1_TXT).toURI());
    this.file2 = new File(this.getClass().getClassLoader().getResource(TEST_FILE2_TXT).toURI());
    this.csvFile = new File(this.getClass().getClassLoader().getResource(TEST_FILE1_CSV).toURI());

    File tmpFile1 = new File(this.file1.getPath().toString() + ".tmp");
    File tmpFile2 = new File(this.file2.getPath().toString() + ".tmp");

    copyFile(this.file1, tmpFile1);
    copyFile(this.file2, tmpFile2);

    this.fileList.add(tmpFile1);
    this.fileList.add(tmpFile2);
  }

  /** @throws Exception */
  @After
  public void tearDown() throws Exception {
    this.file1 = null;
    this.file2 = null;
    this.csvFile = null;
    for (File f : this.fileList) {
      f.delete();
    }
    this.fileList.clear();
    this.fileList = null;
  }

  private static void copyFile(File sourceFile, File destFile) throws IOException {
    if (!destFile.exists()) {
      destFile.createNewFile();
    }

    try (FileInputStream fis = new FileInputStream(sourceFile);
        FileChannel source = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(destFile);
        FileChannel destination = fos.getChannel()) {
      destination.transferFrom(source, 0, source.size());
    }
  }

  public static int estimateTotalSize(String[] mystrings) {
    int total = 0;
    for (String s : mystrings) {
      total += StringSizeEstimator.estimatedSizeOf(s);
    }
    return total;
  }

  public static void oneRoundOfStringSizeEstimation() {
    // could use JMH for better results but this should do
    final int N = 1024;
    String[] mystrings = new String[1024];
    for (int k = 0; k < N; ++k) {
      mystrings[k] = Integer.toString(k);
    }
    final int repeat = 1000;
    long bef, aft, diff;
    long bestdiff = Long.MAX_VALUE;
    int bogus = 0;
    for (int t = 0; t < repeat; ++t) {
      bef = System.nanoTime();
      bogus += estimateTotalSize(mystrings);
      aft = System.nanoTime();
      diff = aft - bef;
      if (diff < bestdiff) bestdiff = diff;
    }
    System.out.println("#ignore = " + bogus);
    System.out.println(
        "[performance] String size estimator uses " + bestdiff * 1.0 / N + " ns per string");
  }

  @Test
  public void stringSizeEstimator() {
    for (int k = 0; k < 10; ++k) {
      oneRoundOfStringSizeEstimation();
    }
  }

  @Test
  public void testEmptyFiles() throws Exception {
    File f1 = File.createTempFile("tmp", "unit");
    File f2 = File.createTempFile("tmp", "unit");
    f1.deleteOnExit();
    f2.deleteOnExit();
    ExternalSort.mergeSortedFiles(ExternalSort.sortInBatch(f1), f2);
    if (f2.length() != 0) throw new RuntimeException("empty files should end up emtpy");
  }

  @Test
  public void testMergeSortedFiles() throws Exception {
    String line;

    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };
    File out = File.createTempFile("test_results", ".tmp", null);
    out.deleteOnExit();
    ExternalSort.mergeSortedFiles(this.fileList, out, cmp, false);

    List<String> result = new ArrayList<>();
    try (BufferedReader bf = new BufferedReader(new FileReader(out))) {
      while ((line = bf.readLine()) != null) {
        result.add(line);
      }
    }
    assertArrayEquals(Arrays.toString(result.toArray()), EXPECTED_MERGE_RESULTS, result.toArray());
  }

  @Test
  public void testMergeSortedFiles_Distinct() throws Exception {
    String line;

    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };
    File out = File.createTempFile("test_results", ".tmp", null);
    out.deleteOnExit();
    ExternalSort.mergeSortedFiles(this.fileList, out, cmp, true);

    List<String> result = new ArrayList<>();
    try (BufferedReader bf = new BufferedReader(new FileReader(out))) {
      while ((line = bf.readLine()) != null) {
        result.add(line);
      }
    }
    assertArrayEquals(
        Arrays.toString(result.toArray()), EXPECTED_MERGE_DISTINCT_RESULTS, result.toArray());
  }

  @Test
  public void testMergeSortedFiles_Append() throws Exception {
    String line;

    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };

    File out = File.createTempFile("test_results", ".tmp", null);
    out.deleteOnExit();
    writeStringToFile(out, "HEADER, HEADER\n");

    ExternalSort.mergeSortedFiles(this.fileList, out, cmp, true, true);

    List<String> result = new ArrayList<>();
    try (BufferedReader bf = new BufferedReader(new FileReader(out))) {
      while ((line = bf.readLine()) != null) {
        result.add(line);
      }
    }
    assertArrayEquals(Arrays.toString(result.toArray()), EXPECTED_HEADER_RESULTS, result.toArray());
  }

  @Test
  public void testSortAndSave() throws Exception {
    File f;
    String line;

    List<String> sample = Arrays.asList(SAMPLE);
    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };
    f = ExternalSort.sortAndSave(sample, cmp, null, false, true);
    assertNotNull(f);
    assertTrue(f.exists());
    assertTrue(f.length() > 0);
    List<String> result = new ArrayList<>();
    try (BufferedReader bf = new BufferedReader(new FileReader(f))) {
      while ((line = bf.readLine()) != null) {
        result.add(line);
      }
    }
    assertArrayEquals(Arrays.toString(result.toArray()), EXPECTED_SORT_RESULTS, result.toArray());
  }

  @Test
  public void testSortAndSave_Distinct() throws Exception {
    File f;
    String line;

    BufferedReader bf;
    List<String> sample = Arrays.asList(SAMPLE);
    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };

    f = ExternalSort.sortAndSave(sample, cmp, null, true, true);
    assertNotNull(f);
    assertTrue(f.exists());
    assertTrue(f.length() > 0);
    bf = new BufferedReader(new FileReader(f));

    List<String> result = new ArrayList<>();
    while ((line = bf.readLine()) != null) {
      result.add(line);
    }
    bf.close();
    assertArrayEquals(
        Arrays.toString(result.toArray()), EXPECTED_DISTINCT_RESULTS, result.toArray());
  }

  @Test
  public void testSortInBatch() throws Exception {
    Comparator<String> cmp =
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        };

    List<File> listOfFiles =
        ExternalSort.sortInBatch(
            this.csvFile, cmp, ExternalSort.DEFAULTMAXTEMPFILES, null, false, true);
    assertEquals(1, listOfFiles.size());

    ArrayList<String> result = readLines(listOfFiles.get(0));
    assertArrayEquals(
        Arrays.toString(result.toArray()), EXPECTED_MERGE_DISTINCT_RESULTS, result.toArray());
  }

  public static ArrayList<String> readLines(File f) throws IOException {
    ArrayList<String> answer;
    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
      answer = new ArrayList<>();
      String line;
      while ((line = r.readLine()) != null) {
        answer.add(line);
      }
    }
    return answer;
  }

  public static void writeStringToFile(File f, String s) throws IOException {
    try (FileOutputStream out = new FileOutputStream(f)) {
      out.write(s.getBytes());
    }
  }

  /**
   * Sort a text file with lines greater than {@link Integer#MAX_VALUE}.
   *
   * @throws IOException
   */
  @Ignore("This test takes too long to execute")
  @Test
  public void sortVeryLargeFile() throws IOException {
    final Path veryLargeFile = getTestFile();
    final Path outputFile = Files.createTempFile("Merged-File", ".tmp");
    final long sortedLines =
        ExternalSort.mergeSortedFiles(
            ExternalSort.sortInBatch(veryLargeFile.toFile()), outputFile.toFile());
    final long expectedLines = 2148L * 1000000L;
    assertEquals(expectedLines, sortedLines);
  }

  /**
   * Generate a test file with 2148 million lines.
   *
   * @throws IOException
   */
  private Path getTestFile() throws IOException {
    System.out.println("Temp File Creation: Started");
    final Path path = Files.createTempFile("IntegrationTestFile", ".txt");
    final List<String> idList = new ArrayList<>();
    final int saneLimit = 1000000;
    IntStream.range(0, saneLimit).forEach(i -> idList.add("A"));
    final String content = idList.stream().collect(Collectors.joining("\n"));
    Files.write(
        path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    final String newLine = "\n";
    IntStream.range(1, 2148)
        .forEach(
            i -> {
              try {
                Files.write(
                    path, newLine.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(
                    path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
              }
            });
    System.out.println("Temp File Creation: Finished");
    return path;
  }

  /**
   * Sort with a custom comparator.
   *
   * @throws IOException
   */
  @Test
  public void sortWithCustomComparator() throws IOException {
    Random rand = new Random();
    final Path path = Files.createTempFile("TestCsvWithLongIds", ".csv");
    final Path pathSorted = Files.createTempFile("TestCsvWithLongIdsSorted", ".csv");
    Set<Long> sortedIds = new TreeSet<>();
    try (FileWriter fw = new FileWriter(path.toFile());
        BufferedWriter bw = new BufferedWriter(fw)) {
      for (int i = 0; i < 1000; ++i) {
        long nextLong = rand.nextLong();
        sortedIds.add(nextLong);
        bw.write(String.format("%d,%s\n", nextLong, UUID.randomUUID().toString()));
      }
    }
    AtomicBoolean wasCalled = new AtomicBoolean(false);
    ExternalSort.sort(
        path.toFile(),
        pathSorted.toFile(),
        (lhs, rhs) -> {
          Long lhsLong =
              lhs.indexOf(',') == -1 ? Long.MAX_VALUE : Long.parseLong(lhs.split(",")[0]);
          Long rhsLong =
              rhs.indexOf(',') == -1 ? Long.MAX_VALUE : Long.parseLong(rhs.split(",")[0]);
          wasCalled.set(true);
          return lhsLong.compareTo(rhsLong);
        });
    assertTrue("The custom comparator was not called!", wasCalled.get());
    Iterator<Long> idIter = sortedIds.iterator();
    try (FileReader fr = new FileReader(pathSorted.toFile());
        BufferedReader bw = new BufferedReader(fr)) {
      String nextLine = bw.readLine();
      Long lhsLong =
          nextLine.indexOf(',') == -1 ? Long.MAX_VALUE : Long.parseLong(nextLine.split(",")[0]);
      Long nextId = idIter.next();
      assertEquals(lhsLong, nextId);
    }
  }

  @Test
  public void lowMaxMemory() throws IOException {
    String unsortedContent =
        "Val1,Data2,Data3,Data4\r\n"
            + "Val2,Data2,Data4,Data5\r\n"
            + "Val1,Data2,Data3,Data5\r\n"
            + "Val2,Data2,Data6,Data7\r\n";
    InputStream bis = new ByteArrayInputStream(unsortedContent.getBytes(StandardCharsets.UTF_8));
    File tmpDirectory = Files.createTempDirectory("sort").toFile();
    tmpDirectory.deleteOnExit();

    BufferedReader inputReader =
        new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));
    List<File> tmpSortedFiles =
        ExternalSort.sortInBatch(
            inputReader,
            unsortedContent.length(),
            ExternalSort.defaultcomparator,
            Integer.MAX_VALUE, // use an unlimited number of temp files
            100, // max memory
            tmpDirectory,
            false, // no distinct
            // no header lines to skip
            // don't use gzip
            true); // parallel
    File tmpOutputFile = File.createTempFile("merged", "", tmpDirectory);
    tmpOutputFile.deleteOnExit();
    BufferedWriter outputWriter =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream((tmpOutputFile))));
    ExternalSort.mergeSortedFiles(
        tmpSortedFiles, outputWriter, ExternalSort.defaultcomparator, false // no distinct
        ); // don't use gzip

    for (File tmpSortedFile : tmpSortedFiles) {
      assertFalse(tmpSortedFile.exists());
    }
  }
}
