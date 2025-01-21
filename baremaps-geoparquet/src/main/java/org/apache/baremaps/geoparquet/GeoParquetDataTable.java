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
import java.net.URI;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.apache.baremaps.store.DataRow;
import org.apache.baremaps.store.DataRowImpl;
import org.apache.baremaps.store.DataSchema;
import org.apache.baremaps.store.DataTable;
import org.apache.hadoop.fs.Path;

public class GeoParquetDataTable implements DataTable {

  private final URI path;

  private DataSchema schema;

  private GeoParquetReader reader;

  public GeoParquetDataTable(URI path) {
    this.path = path;
    this.reader = new GeoParquetReader(new Path(path));
  }

  @Override
  public long size() {
    return reader.size();
  }

  @Override
  public Iterator<DataRow> iterator() {
    return parallelStream().iterator();
  }

  @Override
  public Spliterator<DataRow> spliterator() {
    return parallelStream().spliterator();
  }

  @Override
  public Stream<DataRow> stream() {
    return parallelStream().sequential();
  }

  @Override
  public Stream<DataRow> parallelStream() {
    return reader.readParallel().map(group -> new DataRowImpl(
        GeoParquetTypeConversion.asSchema(path.toString(), group.getGeoParquetSchema()),
        GeoParquetTypeConversion.asRowValues(group)));
  }

  @Override
  public void clear() {
    if (reader != null) {
      reader = null;
    }
    if (schema != null) {
      schema = null;
    }
  }

  @Override
  public DataSchema schema() {
    if (schema == null) {
      this.schema = GeoParquetTypeConversion.asSchema(
          path.toString(),
          reader.getGeoParquetSchema());
      return this.schema;
    }
    return schema;
  }

  public int srid(String column) {
    try {
      return reader.getGeoParquetMetadata().getSrid(column);
    } catch (Exception e) {
      throw new GeoParquetException("Fail to read the SRID from the GeoParquet metadata", e);
    }
  }

  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }
}
