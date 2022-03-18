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

package com.baremaps.baremaps.geonames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Spliterator;
import java.util.function.Consumer;

public class GeonamesSpliterator implements Spliterator<GeonamesRecord> {
  private final BufferedReader reader;

  /** {@inheritdoc} */
  public GeonamesSpliterator(InputStream inputStream) {
    this.reader = new BufferedReader(new InputStreamReader(inputStream));
  }

  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  public int characteristics() {
    return IMMUTABLE;
  }

  public Spliterator<GeonamesRecord> trySplit() {
    return null;
  }

  public boolean tryAdvance(Consumer<? super GeonamesRecord> consumer) {
    try {
      String line = reader.readLine();

      // end if no more line to read
      if (line == null) {
        return false;
      }
      GeonamesRecord geonamesRecord = parse(line);
      consumer.accept(geonamesRecord);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Parse file line and transform it into GeonamesRecord.
   *
   * @param recordLine - Line from Geonames data file
   * @return GeonamesRecord - formatted geonames data
   */
  private static GeonamesRecord parse(String recordLine) {
    String[] a = recordLine.split("\t");
    return new GeonamesRecord(
        Integer.valueOf(a[0]),
        a[1],
        a[2],
        a[3],
        a[4] == null || "".equals(a[4]) ? null : Double.valueOf(a[4]),
        a[4] == null || "".equals(a[5]) ? null : Double.valueOf(a[5]),
        a[6],
        a[7],
        a[8],
        a[9],
        a[10],
        a[11],
        a[12],
        a[13],
        a[14] == null || "".equals(a[14]) ? null : Long.valueOf(a[14]),
        a[15] == null || "".equals(a[15]) ? null : Integer.valueOf(a[15]),
        a[16] == null || "".equals(a[16]) ? null : Integer.valueOf(a[16]),
        a[17],
        a[18]);
  }
}
