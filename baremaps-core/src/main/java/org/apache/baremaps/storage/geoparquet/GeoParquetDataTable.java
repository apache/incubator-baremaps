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

package org.apache.baremaps.storage.geoparquet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.apache.baremaps.data.storage.*;
import org.apache.baremaps.geoparquet.GeoParquetException;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;

public class GeoParquetDataTable implements DataTable {

  private final URI path;

  private DataSchema schema;

  private GeoParquetReader reader;

  public GeoParquetDataTable(URI path) {
    this.path = path;
  }

  private GeoParquetReader reader() {
    if (reader == null) {
      reader = new GeoParquetReader(path);
    }
    return reader;
  }

  @Override
  public long size() {
    try {
      return reader().size();
    } catch (URISyntaxException e) {
      throw new GeoParquetException("Fail to access size from reader", e);
    }
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
    try {
      return reader().read().map(group -> new DataRowImpl(
          GeoParquetTypeConversion.asSchema(path.toString(), group.getSchema()),
          GeoParquetTypeConversion.asRowValues(group)));
    } catch (IOException | URISyntaxException e) {
      throw new GeoParquetException("Fail to read() the reader", e);
    }
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
      try {
        Schema schema = reader().getGeoParquetSchema();
        this.schema = GeoParquetTypeConversion.asSchema(path.toString(), schema);
        return this.schema;
      } catch (URISyntaxException e) {
        throw new GeoParquetException("Failed to get the schema.", e);
      }
    }
    return schema;
  }

  public int srid(String column) {
    try {
      return reader().getGeoParquetMetadata().getSrid(column);
    } catch (Exception e) {
      throw new GeoParquetException("Fail to read the SRID from the GeoParquet metadata", e);
    }
  }
}
