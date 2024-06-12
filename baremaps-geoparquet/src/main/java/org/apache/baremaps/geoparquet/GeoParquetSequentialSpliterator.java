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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.parquet.hadoop.ParquetReader;

public class GeoParquetSequentialSpliterator implements Spliterator<GeoParquetGroup> {

  private ParquetReader<GeoParquetGroup> reader;

  GeoParquetSequentialSpliterator(FileStatus fileStatus, Configuration configuration) {
    try {
      this.reader = ParquetReader
          .builder(new GeoParquetGroupReadSupport(), fileStatus.getPath())
          .withConf(configuration)
          .build();
    } catch (IOException e) {
      throw new GeoParquetException("Failed to create reader for " + fileStatus, e);
    }
  }

  @Override
  public boolean tryAdvance(Consumer<? super GeoParquetGroup> action) {
    try {
      // Read the next group
      GeoParquetGroup group = reader.read();

      // If the group is null, close the resources and set the variables to null
      if (group == null) {
        reader.close();
        return false;
      }

      // Accept the group
      action.accept(group);

      // tell the caller that there are more groups to read
      return true;

    } catch (IOException e) {
      try {
        reader.close();
      } catch (IOException e2) {
        // Ignore the exception as the original exception is more important
      }
      throw new GeoParquetException("IOException caught while trying to read the next file.", e);
    }
  }

  @Override
  public Spliterator<GeoParquetGroup> trySplit() {
    return null;
    // List<GeoParquetGroup> batch = new ArrayList<>();
    // while (batch.size() < 1_000 && tryAdvance(batch::add)) {
    // }
    // if (!batch.isEmpty()) {
    // return Spliterators.spliterator(batch, characteristics() | SIZED);
    // } else {
    // return null;
    // }
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    // The spliterator is not ordered, or sorted
    return NONNULL | DISTINCT | IMMUTABLE | SUBSIZED;
  }
}
