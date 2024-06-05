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
import java.util.Map;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.hadoop.GeoParquetGroupReadSupport;
import org.apache.hadoop.fs.FileStatus;
import org.apache.parquet.hadoop.ParquetReader;

public class GeoParquetGroupSpliterator implements Spliterator<GeoParquetGroup> {

  private final GeoParquetReader geoParquetReader;
  private final Queue<FileStatus> queue;
  private final Map<FileStatus, GeoParquetReader.FileInfo> files;
  private FileStatus fileStatus = null;
  private ParquetReader<GeoParquetGroup> reader;

  GeoParquetGroupSpliterator(GeoParquetReader geoParquetReader,
      Map<FileStatus, GeoParquetReader.FileInfo> files) {
    this.geoParquetReader = geoParquetReader;
    this.files = files;
    this.queue = new ArrayBlockingQueue<>(files.keySet().size(), false, files.keySet());
  }

  @Override
  public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
    try {
      // Poll the next file
      if (fileStatus == null) {
        fileStatus = queue.poll();
      }

      // If there are no more files, return false
      if (fileStatus == null) {
        return false;
      }

      // Create a new reader if it does not exist
      if (reader == null) {
        reader = createParquetReader(fileStatus);
      }

      // Read the next group
      GeoParquetGroup group = reader.read();

      // If the group is null, close the resources and set the variables to null
      if (group == null) {
        reader.close();
        reader = null;
        fileStatus = null;

        // Try to advance again
        return tryAdvance(action);
      }

      // Accept the group and tell the caller that there are more groups to read
      action.accept(group);
      return true;

    } catch (IOException e) {
      // If an exception occurs, try to close the resources and throw a runtime exception
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e2) {
          // Ignore the exception as the original exception is more important
        }
        reader = null;
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
    if (queue.size() < 2) {
      // There is nothing left to split
      return null;
    }

    // Create a new spliterator by polling the next polledFileStatus
    FileStatus polledFileStatus = queue.poll();

    // If there are no more files, tell the caller that there is nothing to split anymore
    if (polledFileStatus == null) {
      return null;
    }

    // Return a new spliterator with the polledFileStatus
    return new GeoParquetGroupSpliterator(geoParquetReader,
        Map.of(polledFileStatus, files.get(polledFileStatus)));
  }

  @Override
  public long estimateSize() {
    return files.values().stream()
        .map(GeoParquetReader.FileInfo::recordCount)
        .reduce(0L, Long::sum);
  }

  @Override
  public int characteristics() {
    // The spliterator is not ordered, or sorted
    return NONNULL | IMMUTABLE | SIZED | DISTINCT;
  }
}
