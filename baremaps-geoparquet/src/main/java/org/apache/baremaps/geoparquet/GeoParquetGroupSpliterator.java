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
import java.util.*;
import java.util.function.Consumer;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.hadoop.GeoParquetGroupReadSupport;
import org.apache.hadoop.fs.FileStatus;
import org.apache.parquet.hadoop.ParquetReader;

class GeoParquetGroupSpliterator implements Spliterator<GeoParquetGroup> {

  private final GeoParquetReader geoParquetReader;
  private final List<FileStatus> fileStatuses;
  private ParquetReader<GeoParquetGroup> reader;

  GeoParquetGroupSpliterator(GeoParquetReader geoParquetReader, List<FileStatus> files) {
    this.geoParquetReader = geoParquetReader;
    this.fileStatuses = Collections.synchronizedList(files);
    setupReaderForNextFile();
  }

  private void setupReaderForNextFile() {
    FileStatus fileStatus = fileStatuses.remove(0);
    try {
      reader = createParquetReader(fileStatus);
    } catch (IOException e) {
      throw new GeoParquetException("Failed to create reader for " + fileStatus, e);
    }
  }

  @Override
  public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
    try {
      // Read the next group
      GeoParquetGroup group = reader.read();

      // If the group is null, try to get the one from the next file.
      while (group == null) {
        synchronized (fileStatuses) {
          if (fileStatuses.isEmpty()) {
            reader.close();
            return false;
          }
          setupReaderForNextFile();
        }
        group = reader.read();
      }

      // Accept the group and tell the caller that there are more groups to read
      action.accept(group);
      return true;

    } catch (IOException e) {
      // If an exception occurs, try to close the resources and throw a runtime exception
      try {
        reader.close();
      } catch (IOException e2) {
        // Ignore the exception as the original exception is more important
      }
      throw new GeoParquetException("IOException caught while trying to read the next file.", e);
    }
  }

  private ParquetReader<GeoParquetGroup> createParquetReader(FileStatus file)
      throws IOException {
    return ParquetReader
        .builder(new GeoParquetGroupReadSupport(), file.getPath())
        .withConf(geoParquetReader.configuration)
        .build();
  }

  @Override
  public Spliterator<GeoParquetGroup> trySplit() {
    List<FileStatus> sublist;
    synchronized (fileStatuses) {
      if (fileStatuses.size() < 2) {
        // There is nothing left to split
        return null;
      }

      sublist = fileStatuses.subList(0, fileStatuses.size() / 2);
    }
    List<FileStatus> secondList = new ArrayList<>(sublist);
    sublist.clear();

    // Return a new spliterator with the sublist
    return new GeoParquetGroupSpliterator(geoParquetReader, secondList);
  }

  @Override
  public long estimateSize() {
    return geoParquetReader.size();
  }

  @Override
  public int characteristics() {
    // The spliterator is not ordered, or sorted
    return NONNULL | CONCURRENT | DISTINCT;
  }
}
