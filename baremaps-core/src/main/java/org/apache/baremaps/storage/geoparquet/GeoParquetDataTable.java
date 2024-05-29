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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.apache.baremaps.data.schema.*;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;

public class GeoParquetDataTable implements DataTable {

  private final URI path;

  private DataRowType rowType;

  private GeoParquetReader reader;

  public GeoParquetDataTable(URI path) {
    this.path = path;
  }

  private GeoParquetReader reader() {
    if (reader == null) {
      try {
        reader = new GeoParquetReader(path);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return reader;
  }

  @Override
  public long size() {
    try {
      return reader().size();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
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
          GeoParquetTypeConversion.asDataRowType(path.toString(), group.getSchema()),
          GeoParquetTypeConversion.asDataRow(group)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void clear() {

  }

  @Override
  public DataRowType rowType() {
    if (rowType == null) {
      try {
        Schema schema = reader().getGeoParquetSchema();
        rowType = GeoParquetTypeConversion.asDataRowType(path.toString(), schema);
        return rowType;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return rowType;
  }
}
