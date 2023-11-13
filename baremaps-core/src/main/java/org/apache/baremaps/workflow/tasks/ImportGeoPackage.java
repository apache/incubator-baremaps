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

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.nio.file.Path;
import org.apache.baremaps.database.schema.DataTableAdapter;
import org.apache.baremaps.database.schema.DataTableGeometryTransformer;
import org.apache.baremaps.storage.geopackage.GeoPackageDataSchema;
import org.apache.baremaps.storage.postgres.PostgresDataSchema;
import org.apache.baremaps.utils.ProjectionTransformer;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import a GeoPackage into a database.
 */
@JsonTypeName("ImportGeoPackage")
public class ImportGeoPackage implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportGeoPackage.class);

  private Path file;
  private Object database;
  private Integer sourceSRID;
  private Integer targetSRID;

  /**
   * Constructs an {@code ImportGeoPackage}.
   */
  public ImportGeoPackage() {

  }

  /**
   * Constructs an {@code ImportGeoPackage}.
   *
   * @param file the GeoPackage file
   * @param database the database
   * @param sourceSRID the source SRID
   * @param targetSRID the target SRID
   */
  public ImportGeoPackage(Path file, Object database, Integer sourceSRID, Integer targetSRID) {
    this.file = file;
    this.database = database;
    this.sourceSRID = sourceSRID;
    this.targetSRID = targetSRID;
  }

  /**
   * Returns the GeoPackage file.
   *
   * @return the GeoPackage file
   */
  public Path getFile() {
    return file;
  }

  /**
   * Sets the GeoPackage file.
   *
   * @param file the GeoPackage file
   */
  public void setFile(Path file) {
    this.file = file;
  }

  /**
   * Returns the database.
   *
   * @return the database
   */
  public Object getDatabase() {
    return database;
  }

  /**
   * Sets the database.
   *
   * @param database the database
   */
  public void setDatabase(Object database) {
    this.database = database;
  }

  /**
   * Returns the source SRID.
   *
   * @return the source SRID
   */
  public Integer getSourceSRID() {
    return sourceSRID;
  }

  /**
   * Sets the source SRID.
   *
   * @param sourceSRID the source SRID
   */
  public void setSourceSRID(Integer sourceSRID) {
    this.sourceSRID = sourceSRID;
  }

  /**
   * Returns the target SRID.
   *
   * @return the target SRID
   */
  public Integer getTargetSRID() {
    return targetSRID;
  }

  /**
   * Sets the target SRID.
   *
   * @param targetSRID the target SRID
   */
  public void setTargetSRID(Integer targetSRID) {
    this.targetSRID = targetSRID;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();
    try (var geoPackageDataStore = new GeoPackageDataSchema(path)) {
      var dataSource = context.getDataSource(database);
      var postgresDataStore = new PostgresDataSchema(dataSource);
      for (var name : geoPackageDataStore.list()) {
        var geoPackageTable = geoPackageDataStore.get(name);
        var projectionTransformer = new ProjectionTransformer(sourceSRID, targetSRID);
        var rowTransformer =
            new DataTableGeometryTransformer(geoPackageTable, projectionTransformer);
        var transformedDataTable =
            new DataTableAdapter(geoPackageDataStore.get(name), rowTransformer);
        postgresDataStore.add(transformedDataTable);
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
