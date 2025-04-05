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

import java.nio.file.Path;
import java.util.StringJoiner;
import org.apache.baremaps.calcite.DataTableGeometryMapper;
import org.apache.baremaps.calcite.DataTableMapper;
import org.apache.baremaps.calcite.postgres.PostgresDataStore;
import org.apache.baremaps.calcite.shapefile.ShapefileDataTable;
import org.apache.baremaps.openstreetmap.function.ProjectionTransformer;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import a shapefile into a database.
 */
public class ImportShapefile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportShapefile.class);

  private Path file;
  private Integer fileSrid;
  private Object database;
  private Integer databaseSrid;

  /**
   * Constructs a {@code ImportShapefile}.
   */
  public ImportShapefile() {

  }

  /**
   * Constructs an {@code ImportShapefile}.
   *
   * @param file the shapefile file
   * @param fileSrid the source SRID
   * @param database the database
   * @param databaseSrid the target SRID
   */
  public ImportShapefile(Path file, Integer fileSrid, Object database, Integer databaseSrid) {
    this.file = file;
    this.fileSrid = fileSrid;
    this.database = database;
    this.databaseSrid = databaseSrid;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();
    try {
      var shapefileDataTable = new ShapefileDataTable(path);
      var dataSource = context.getDataSource(database);
      var postgresDataStore = new PostgresDataStore(dataSource);
      var rowTransformer = new DataTableGeometryMapper(shapefileDataTable,
          new ProjectionTransformer(fileSrid, databaseSrid));
      var transformedDataTable = new DataTableMapper(shapefileDataTable, rowTransformer);
      postgresDataStore.add(transformedDataTable);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", ImportShapefile.class.getSimpleName() + "[", "]")
        .add("file=" + file)
        .add("fileSrid=" + fileSrid)
        .add("database=" + database)
        .add("databaseSrid=" + databaseSrid)
        .toString();
  }
}
