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

package org.apache.baremaps.calcite2.postgis;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

import org.apache.baremaps.calcite2.data.DataColumn;
import org.apache.baremaps.calcite2.data.DataColumnFixed;
import org.apache.baremaps.calcite2.data.DataRow;
import org.apache.baremaps.calcite2.data.DataSchema;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.ColumnResult;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

/**
 * A Calcite table implementation for PostGIS. This table allows querying spatial data
 * directly from a PostGIS-enabled PostgreSQL database.
 */
public class PostgisTable extends AbstractTable implements ScannableTable {

  private final DataSource dataSource;
  private final String tableName;
  private final RelDataType rowType;
  private final DataSchema dataSchema;

  /**
   * Constructs a PostgisTable with the specified data source and table name.
   *
   * @param dataSource the data source for the PostgreSQL connection
   * @param tableName the name of the table to access
   * @throws SQLException if an SQL error occurs
   */
  public PostgisTable(DataSource dataSource, String tableName) throws SQLException {
    this(dataSource, tableName, new org.apache.calcite.jdbc.JavaTypeFactoryImpl());
  }

  /**
   * Constructs a PostgisTable with the specified data source, table name, and type factory.
   *
   * @param dataSource the data source for the PostgreSQL connection
   * @param tableName the name of the table to access
   * @param typeFactory the type factory
   * @throws SQLException if an SQL error occurs
   */
  public PostgisTable(DataSource dataSource, String tableName, RelDataTypeFactory typeFactory) 
      throws SQLException {
    this.dataSource = dataSource;
    this.tableName = tableName;
    this.dataSchema = discoverSchema();
    this.rowType = PostgisTypeConversion.toRelDataType(typeFactory, dataSchema);
  }

  /**
   * Discovers the schema of the PostGIS table using DatabaseMetadata.
   *
   * @return the schema of the table
   * @throws SQLException if an SQL error occurs
   */
  private DataSchema discoverSchema() throws SQLException {
    List<DataColumn> columns = new ArrayList<>();
    
    // Use DatabaseMetadata to get column information
    DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
    var tableMetadata = metadata.getTableMetaData(null, null, tableName, new String[]{"TABLE", "VIEW"})
        .stream()
        .filter(meta -> meta.table().tableName().equalsIgnoreCase(tableName))
        .findFirst()
        .orElseThrow(() -> new SQLException("Table not found: " + tableName));
    
    // Get geometry column information separately since it's PostGIS specific
    Map<String, String> geometryTypes = getGeometryTypes();
    
    for (ColumnResult column : tableMetadata.columns()) {
      String columnName = column.columnName();
      String dataType = column.typeName();
      boolean isNullable = "YES".equalsIgnoreCase(column.isNullable());
      
      // Determine column cardinality
      DataColumn.Cardinality cardinality = isNullable ? 
          DataColumn.Cardinality.OPTIONAL : DataColumn.Cardinality.REQUIRED;
      
      // Create a data column based on the type
      RelDataTypeFactory typeFactory = new org.apache.calcite.jdbc.JavaTypeFactoryImpl();
      RelDataType relDataType;
      
      if ("geometry".equalsIgnoreCase(dataType) && geometryTypes.containsKey(columnName)) {
        // This is a geometry column
        relDataType = typeFactory.createSqlType(SqlTypeName.GEOMETRY);
      } else {
        // Handle regular PostgreSQL types
        relDataType = PostgisTypeConversion.postgresTypeToRelDataType(
            typeFactory, dataType);
      }
      
      columns.add(new DataColumnFixed(columnName, cardinality, relDataType));
    }
    
    return new DataSchema(tableName, columns);
  }
  
  /**
   * Gets geometry column types for the current table.
   *
   * @return a map of column names to geometry types
   * @throws SQLException if an SQL error occurs
   */
  private Map<String, String> getGeometryTypes() throws SQLException {
    Map<String, String> geometryTypes = new HashMap<>();
    
    try (Connection connection = dataSource.getConnection()) {
      // Query to get geometry column information
      String sql = "SELECT f_geometry_column, type FROM geometry_columns WHERE f_table_name = ?";
      
      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, tableName);
        
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            String column = rs.getString("f_geometry_column");
            String type = rs.getString("type");
            geometryTypes.put(column, type);
          }
        }
      }
    }
    
    return geometryTypes;
  }
  
  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    return rowType;
  }

  /**
   * Returns the schema of this table.
   *
   * @return the schema of the table
   */
  public DataSchema schema() {
    return dataSchema;
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    return new AbstractEnumerable<>() {
      @Override
      public Enumerator<Object[]> enumerator() {
        return new PostgisEnumerator(dataSource, dataSchema);
      }
    };
  }

  /**
   * Enumerator for PostGIS data.
   */
  private static class PostgisEnumerator implements Enumerator<Object[]> {
    private final DataSource dataSource;
    private final DataSchema schema;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private boolean hasNext;
    private Object[] current;

    /**
     * Constructs a PostgisEnumerator with the specified data source and schema.
     *
     * @param dataSource the data source
     * @param schema the schema
     */
    public PostgisEnumerator(DataSource dataSource, DataSchema schema) {
      this.dataSource = dataSource;
      this.schema = schema;
      this.current = null;
      try {
        this.connection = dataSource.getConnection();
        this.statement = connection.createStatement();
        this.resultSet = statement.executeQuery(buildSelectQuery());
        this.hasNext = resultSet.next();
        if (this.hasNext) {
          this.current = convertCurrentRow();
        }
      } catch (SQLException e) {
        close();
        throw new RuntimeException("Error initializing PostGIS query", e);
      }
    }

    private String buildSelectQuery() {
      List<String> columnProjections = new ArrayList<>();
      
      for (DataColumn column : schema.columns()) {
        // Special handling for geometry columns to get WKB format
        if (column.sqlTypeName() == SqlTypeName.GEOMETRY) {
          columnProjections.add(String.format("ST_AsBinary(\"%s\") AS \"%s\"", 
              column.name(), column.name()));
        } else {
          columnProjections.add(String.format("\"%s\"", column.name()));
        }
      }
      
      return "SELECT " + String.join(", ", columnProjections) + 
          " FROM \"" + schema.name() + "\"";
    }

    private Object[] convertCurrentRow() throws SQLException {
      Object[] values = new Object[schema.columns().size()];
      for (int i = 0; i < schema.columns().size(); i++) {
        DataColumn column = schema.columns().get(i);
        Object value;
        
        // Special handling for geometry columns
        if (column.sqlTypeName() == SqlTypeName.GEOMETRY) {
          byte[] wkb = resultSet.getBytes(i + 1);
          value = deserializeWkb(wkb);
        } else {
          value = resultSet.getObject(i + 1);
        }
        
        values[i] = value;
      }
      return values;
    }

    private Geometry deserializeWkb(byte[] wkb) {
      if (wkb == null) {
        return null;
      }
      try {
        WKBReader reader = new WKBReader(new GeometryFactory());
        return reader.read(wkb);
      } catch (ParseException e) {
        throw new RuntimeException("Error parsing WKB geometry", e);
      }
    }

    @Override
    public Object[] current() {
      return current;
    }

    @Override
    public boolean moveNext() {
      try {
        if (!hasNext) {
          current = null;
          return false;
        }
        
        current = convertCurrentRow();
        hasNext = resultSet.next();
        return true;
      } catch (SQLException e) {
        close();
        throw new RuntimeException("Error fetching next result", e);
      }
    }

    @Override
    public void reset() {
      close();
      try {
        this.connection = dataSource.getConnection();
        this.statement = connection.createStatement();
        this.resultSet = statement.executeQuery(buildSelectQuery());
        this.hasNext = resultSet.next();
        if (this.hasNext) {
          this.current = convertCurrentRow();
        }
      } catch (SQLException e) {
        close();
        throw new RuntimeException("Error resetting PostGIS query", e);
      }
    }

    @Override
    public void close() {
      try {
        if (resultSet != null) {
          resultSet.close();
          resultSet = null;
        }
        if (statement != null) {
          statement.close();
          statement = null;
        }
        if (connection != null) {
          connection.close();
          connection = null;
        }
      } catch (SQLException e) {
        throw new RuntimeException("Error closing resources", e);
      }
    }
  }
} 