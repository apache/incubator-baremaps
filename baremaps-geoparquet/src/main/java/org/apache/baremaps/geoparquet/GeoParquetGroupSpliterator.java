/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.geoparquet;

import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.hadoop.GeoParquetGroupReadSupport;
import org.apache.hadoop.fs.FileStatus;
import org.apache.parquet.hadoop.ParquetReader;

class GeoParquetGroupSpliterator implements Spliterator<GeoParquetGroup> {

  private final GeoParquetReader geoParquetReader;
  private final List<FileStatus> fileStatuses;
  private int currentFileIndex;
  private int currentEndIndex;
  private ParquetReader<GeoParquetGroup> reader;

  public GeoParquetGroupSpliterator(GeoParquetReader geoParquetReader, List<FileStatus> files) {
    this(geoParquetReader, files, 0, files.size());
  }

  GeoParquetGroupSpliterator(
      GeoParquetReader geoParquetReader,
      List<FileStatus> fileStatuses,
      int startIndex,
      int endIndex) {
    this.geoParquetReader = geoParquetReader;
    this.fileStatuses = fileStatuses;
    this.currentFileIndex = startIndex;
    this.currentEndIndex = endIndex;
    setupReaderForNextFile();
  }



  private void setupReaderForNextFile() {
    closeCurrentReader();

    if (currentFileIndex >= currentEndIndex) {
      reader = null;
      return;
    }

    FileStatus fileStatus = fileStatuses.get(currentFileIndex++);
    try {
      reader = createParquetReader(fileStatus);
    } catch (IOException e) {
      throw new GeoParquetException("Failed to create reader for " + fileStatus, e);
    }
  }

  private void closeCurrentReader() {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        // Ignore exceptions during close
      }
      reader = null;
    }
  }

  @Override
  public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
    try {
      while (true) {
        if (reader == null) {
          return false;
        }

        GeoParquetGroup group = reader.read();

        if (group == null) {
          setupReaderForNextFile();
          continue;
        }

        action.accept(group);
        return true;
      }
    } catch (IOException e) {
      closeCurrentReader();
      throw new GeoParquetException("IOException caught while trying to read the next file.", e);
    }
  }

  private ParquetReader<GeoParquetGroup> createParquetReader(FileStatus file)
      throws IOException {
    return ParquetReader.builder(new GeoParquetGroupReadSupport(), file.getPath())
        .withConf(geoParquetReader.getConfiguration())
        .build();
  }

  @Override
  public Spliterator<GeoParquetGroup> trySplit() {
    int remainingFiles = currentEndIndex - currentFileIndex;
    if (remainingFiles <= 1) {
      return null;
    }
    int mid = currentFileIndex + remainingFiles / 2;
    GeoParquetGroupSpliterator split = new GeoParquetGroupSpliterator(
        geoParquetReader, fileStatuses, mid, currentEndIndex);
    this.currentEndIndex = mid;
    return split;
  }

  @Override
  public long estimateSize() {
    // Return Long.MAX_VALUE as the actual number of elements is unknown
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return NONNULL | IMMUTABLE;
  }
}
