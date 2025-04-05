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

package org.apache.baremaps.tasks;

import java.net.URI;
import java.util.StringJoiner;
import org.apache.baremaps.calcite.DataTableGeometryMapper;
import org.apache.baremaps.calcite.DataTableMapper;
import org.apache.baremaps.calcite.geoparquet.GeoParquetDataStore;
import org.apache.baremaps.calcite.geoparquet.GeoParquetDataTable;
import org.apache.baremaps.calcite.postgres.PostgresDataStore;
import org.apache.baremaps.openstreetmap.function.ProjectionTransformer;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import a GeoParquet into a database.
 */
public class ImportGeoParquet implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportGeoParquet.class);

  private URI uri;
  private String tableName;
  private Object database;
  private Integer databaseSrid;

  /**
   * Constructs a {@code ImportGeoParquet}.
   */
  public ImportGeoParquet() {

  }

  /**
   * Constructs an {@code ImportGeoParquet}.
   *
   * @param uri the GeoParquet uri
   * @param database the database
   * @param databaseSrid the target SRID
   */
  public ImportGeoParquet(URI uri, String tableName, Object database, Integer databaseSrid) {
    this.uri = uri;
    this.tableName = tableName;
    this.database = database;
    this.databaseSrid = databaseSrid;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var geoParquetDataStore = new GeoParquetDataStore(uri);
    var dataSource = context.getDataSource(database);
    var postgresDataStore = new PostgresDataStore(dataSource);
    for (var name : geoParquetDataStore.list()) {
      var geoParquetTable = (GeoParquetDataTable) geoParquetDataStore.get(name);
      var projectionTransformer =
          new ProjectionTransformer(geoParquetTable.srid("geometry"), databaseSrid);
      var rowTransformer =
          new DataTableGeometryMapper(geoParquetTable, projectionTransformer);
      var transformedDataTable =
          new DataTableMapper(geoParquetDataStore.get(name), rowTransformer);
      postgresDataStore.add(tableName, transformedDataTable);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", ImportGeoParquet.class.getSimpleName() + "[", "]")
        .add("uri=" + uri)
        .add("database=" + database)
        .add("databaseSrid=" + databaseSrid)
        .toString();
  }
}
