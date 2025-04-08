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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import javax.sql.DataSource;
import org.apache.baremaps.calcite.postgres.PostgresDdlExecutor;
import org.apache.baremaps.calcite.shapefile.ShapefileTable;
import org.apache.baremaps.shapefile.DBaseFieldDescriptor;
import org.apache.baremaps.shapefile.ShapefileReader;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.locationtech.jts.geom.Geometry;
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
    var dataSource = context.getDataSource(database);
    var tableName = file.getFileName().toString().replaceFirst("[.][^.]+$", "").toLowerCase();

    // Create a ShapefileTable to get schema information
    var shapefileTable = new ShapefileTable(path.toFile());
    var shapefileReader = new ShapefileReader(path.toString());
    var fieldDescriptors = shapefileReader.getDatabaseFieldsDescriptors();

    // Create table using PostgresDdlExecutor
    createTable(dataSource, tableName, fieldDescriptors);

    // Import data using JDBC batch inserts
    importData(dataSource, tableName, shapefileReader);
  }

  private void createTable(DataSource dataSource, String tableName, List<DBaseFieldDescriptor> fieldDescriptors) throws Exception {
    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");
    info.setProperty("caseSensitive", "false");
    info.setProperty("parserFactory", PostgresDdlExecutor.class.getName() + "#PARSER_FACTORY");

    try (Connection connection = dataSource.getConnection()) {
      // First ensure PostGIS extension is available
      try (Statement stmt = connection.createStatement()) {
        stmt.execute("CREATE EXTENSION IF NOT EXISTS postgis");
      }

      // Create table DDL
      StringBuilder ddl = new StringBuilder();
      ddl.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
      
      // Add columns from shapefile
      for (DBaseFieldDescriptor field : fieldDescriptors) {
        String sqlType = getSqlType(field);
        ddl.append(field.getName()).append(" ").append(sqlType).append(", ");
      }

      // Add geometry column
      ddl.append("geometry geometry)");

      // Execute CREATE TABLE
      try (Statement stmt = connection.createStatement()) {
        stmt.execute(ddl.toString());
      }

      // Set SRID on geometry column if specified
      if (databaseSrid != null) {
        try (Statement stmt = connection.createStatement()) {
          stmt.execute(String.format(
              "SELECT UpdateGeometrySRID('%s', 'geometry', %d)",
              tableName, databaseSrid));
        }
      }
    }
  }

  private void importData(DataSource dataSource, String tableName, ShapefileReader shapefileReader) throws Exception {
    try (Connection connection = dataSource.getConnection();
         var shapefileInputStream = shapefileReader.read()) {

      // Build INSERT statement
      StringBuilder sql = new StringBuilder();
      sql.append("INSERT INTO ").append(tableName).append(" VALUES (");
      var fieldDescriptors = shapefileReader.getDatabaseFieldsDescriptors();
      for (int i = 0; i < fieldDescriptors.size(); i++) {
        sql.append("?, ");
      }
      sql.append("ST_Transform(ST_SetSRID(?, ?), ?))");

      try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
        List<Object> row;
        int batchSize = 0;
        final int MAX_BATCH = 1000;

        while ((row = shapefileInputStream.readRow()) != null) {
          // Set field values
          for (int i = 0; i < row.size() - 1; i++) {
            pstmt.setObject(i + 1, row.get(i));
          }

          // Set geometry with SRID transformation
          Geometry geom = (Geometry) row.get(row.size() - 1);
          pstmt.setString(row.size(), geom.toText());
          pstmt.setInt(row.size() + 1, fileSrid);
          pstmt.setInt(row.size() + 2, databaseSrid);

          pstmt.addBatch();
          batchSize++;

          if (batchSize >= MAX_BATCH) {
            pstmt.executeBatch();
            batchSize = 0;
          }
        }

        if (batchSize > 0) {
          pstmt.executeBatch();
        }
      }
    }
  }

  private String getSqlType(DBaseFieldDescriptor field) {
    return switch (field.getType()) {
      case CHARACTER -> "VARCHAR";
      case NUMBER -> field.getDecimalCount() == 0 ? "BIGINT" : "DOUBLE PRECISION";
      case CURRENCY, DOUBLE, FLOATING_POINT -> "DOUBLE PRECISION";
      case INTEGER, AUTO_INCREMENT -> "INTEGER";
      case LOGICAL -> "BOOLEAN";
      case DATE -> "DATE";
      case MEMO -> "TEXT";
      case TIMESTAMP, DATE_TIME -> "TIMESTAMP";
      default -> "VARCHAR";
    };
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
