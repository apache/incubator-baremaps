/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.sis.internal.shapefile.jdbc.metadata;

import java.io.File;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.AbstractJDBC;
import org.apache.sis.internal.shapefile.jdbc.SQLConnectionClosedException;
import org.apache.sis.internal.shapefile.jdbc.connection.DBFConnection;
import org.apache.sis.internal.shapefile.jdbc.resultset.*;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * ResultSet Metadata.
 *
 * @author Marc LE BIHAN
 */
public class DBFResultSetMataData extends AbstractJDBC implements ResultSetMetaData {
  /** ResultSet. */
  private DBFRecordBasedResultSet rs;

  /** Database metadata. */
  private DBFDatabaseMetaData metadata;

  /**
   * Construct a ResultSetMetaData.
   *
   * @param resultset ResultSet.
   */
  public DBFResultSetMataData(DBFRecordBasedResultSet resultset) {
    Objects.requireNonNull(resultset, "A non null ResultSet is required.");
    this.rs = resultset;

    try {
      this.metadata = (DBFDatabaseMetaData) resultset.getStatement().getConnection().getMetaData();
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /** @see java.sql.Wrapper#unwrap(java.lang.Class) */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLFeatureNotSupportedException {
    throw unsupportedOperation("unwrap", iface);
  }

  /** @see java.sql.Wrapper#isWrapperFor(java.lang.Class) */
  @Override
  public boolean isWrapperFor(Class<?> iface) {
    logStep("isWrapperFor", iface);
    return iface.isAssignableFrom(getInterface());
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnCount()
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @SuppressWarnings("resource") // The current connection is only used and has not to be closed.
  @Override
  public int getColumnCount() throws SQLConnectionClosedException {
    logStep("getColumnCount");
    DBFConnection cnt = (DBFConnection) (((DBFStatement) this.rs.getStatement()).getConnection());

    return cnt.getColumnCount();
  }

  /**
   * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public boolean isAutoIncrement(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("isAutoIncrement", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getString("TYPE_NAME").equals("AUTO_INCREMENT");
    } catch (SQLNoSuchFieldException e) {
      // We encounter an internal API error in this case.
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "TYPE_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see java.sql.ResultSetMetaData#isCaseSensitive(int) */
  @Override
  public boolean isCaseSensitive(int column) {
    logStep("isCaseSensitive", column);
    return true; // Yes, because behind, there's a HashMap.
  }

  /** @see java.sql.ResultSetMetaData#isSearchable(int) */
  @Override
  public boolean isSearchable(int column) {
    logStep("isSearchable", column);
    return true; // All currently are searcheable.
  }

  /**
   * @see java.sql.ResultSetMetaData#isCurrency(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public boolean isCurrency(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("isCurrency", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getString("TYPE_NAME").equals("CURRENCY");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "TYPE_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see java.sql.ResultSetMetaData#isNullable(int) */
  @Override
  public int isNullable(int column) {
    logStep("isNullable", column);
    return ResultSetMetaData
        .columnNullableUnknown; // TODO Check if somes settings exists for that in field descriptor.
  }

  /** @see java.sql.ResultSetMetaData#isSigned(int) */
  @Override
  public boolean isSigned(int column) {
    logStep("isSigned", column);
    return true; // TODO Check if somes settings exists for that in field descriptor.
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public int getColumnDisplaySize(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getColumnDisplaySize", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      switch (rsDatabase.getString("TYPE_NAME")) {
        case "AUTO_INCREMENT":
        case "CHAR":
        case "INTEGER":
          return rsDatabase.getInt("COLUMN_SIZE");

        case "DATE":
          return 8;

          // Add decimal separator for decimal numbers.
        case "DOUBLE":
        case "FLOAT":
        case "DECIMAL":
          return rsDatabase.getInt("COLUMN_SIZE") + 1;

        case "BOOLEAN":
          return 5; // Translation for true, false, null.

          // Unhandled types default to field length.
        case "CURRENCY":
        case "DATETIME":
        case "TIMESTAMP":
        case "MEMO":
        case "PICTURE":
        case "VARIFIELD":
        case "VARIANT":
        case "UNKNOWN":
          return rsDatabase.getInt("COLUMN_SIZE");

        default:
          return rsDatabase.getInt("COLUMN_SIZE");
      }
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "TYPE_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnLabel(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public String getColumnLabel(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getColumnLabel", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getString("COLUMN_NAME");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "COLUMN_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnName(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public String getColumnName(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getColumnName", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getString("COLUMN_NAME");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "COLUMN_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see java.sql.ResultSetMetaData#getSchemaName(int) */
  @Override
  public String getSchemaName(int column) {
    logStep("getSchemaName", column);
    return ""; // No schema name in DBase 3.
  }

  /**
   * @see java.sql.ResultSetMetaData#getPrecision(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public int getPrecision(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getPrecision", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getInt("COLUMN_SIZE");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "COLUMN_SIZE",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /**
   * @see java.sql.ResultSetMetaData#getScale(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public int getScale(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getScale", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getInt("DECIMAL_DIGITS");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "DECIMAL_DIGITS",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see java.sql.ResultSetMetaData#getTableName(int) */
  @Override
  public String getTableName(int column) {
    logStep("getTableName", column);

    // The table default to the file name (without its extension .dbf).
    String fileName = this.rs.getFile().getName();
    int indexDBF = fileName.lastIndexOf(".");
    String tableName = fileName.substring(0, indexDBF);

    return tableName;
  }

  /** @see java.sql.ResultSetMetaData#getCatalogName(int) */
  @Override
  public String getCatalogName(int column) {
    logStep("getCatalogName", column);
    return "";
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnType(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   */
  @Override
  public int getColumnType(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getColumnType", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getInt("DATA_TYPE");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "DATA_TYPE",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public String getColumnTypeName(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    logStep("getColumnTypeName", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      return rsDatabase.getString("TYPE_NAME");
    } catch (SQLNoSuchFieldException e) {
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "TYPE_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see java.sql.ResultSetMetaData#isReadOnly(int) */
  @Override
  public boolean isReadOnly(int column) {
    logStep("isReadOnly", column);
    return false; // TODO Check if somes settings exists for that in field descriptor.
  }

  /** @see java.sql.ResultSetMetaData#isWritable(int) */
  @Override
  public boolean isWritable(int column) {
    logStep("isWritable", column);
    return true; // TODO Check if somes settings exists for that in field descriptor.
  }

  /** @see java.sql.ResultSetMetaData#isDefinitelyWritable(int) */
  @Override
  public boolean isDefinitelyWritable(int column) {
    logStep("isDefinitelyWritable", column);
    return true; // TODO Check if somes settings exists for that in field descriptor.
  }

  /**
   * @see java.sql.ResultSetMetaData#getColumnClassName(int)
   * @throws SQLFeatureNotSupportedException if underlying class implementing a type isn't currently
   *     set.
   * @throws SQLIllegalColumnIndexException if the column index is illegal.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public String getColumnClassName(int column)
      throws SQLFeatureNotSupportedException, SQLIllegalColumnIndexException,
          SQLConnectionClosedException {
    logStep("getColumnClassName", column);

    try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase = desc(column)) {
      switch (rsDatabase.getString("TYPE_NAME")) {
        case "AUTO_INCREMENT":
          return Integer.class.getName();

        case "CHAR":
          return String.class.getName();

        case "INTEGER":
          return Integer.class.getName();

        case "DATE":
          return java.sql.Date.class.getName();

        case "DOUBLE":
          return Double.class.getName();

        case "FLOAT":
          return Float.class.getName();

        case "DECIMAL":
          return Double.class.getName();

        case "BOOLEAN":
          return Boolean.class.getName();

        case "CURRENCY":
          return Double.class.getName();

        case "DATETIME":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on DateTime");

        case "TIMESTAMP":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on TimeStamp");

        case "MEMO":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on Memo");

        case "PICTURE":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on Picture");

        case "VARIFIELD":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on VariField");

        case "VARIANT":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on Variant");

        case "UNKNOWN":
          throw unsupportedOperation(
              "ResultSetMetaData.getColumnClassName(..) on " + rsDatabase.getString("TYPE_NAME"));

        default:
          throw unsupportedOperation(
              "ResultSetMetaData.getColumnClassName(..) on " + rsDatabase.getString("TYPE_NAME"));
      }
    } catch (SQLNoSuchFieldException e) {
      // We encounter an internal API error in this case.
      String message =
          format(
              Level.SEVERE,
              "assert.expected_databasemetadata_not_found",
              "TYPE_NAME",
              e.getMessage());
      throw new RuntimeException(message, e);
    }
  }

  /** @see org.apache.sis.internal.shapefile.jdbc.AbstractJDBC#getInterface() */
  @Override
  protected Class<?> getInterface() {
    return ResultSetMetaData.class;
  }

  /** @see org.apache.sis.internal.shapefile.jdbc.AbstractJDBC#getFile() */
  @Override
  protected File getFile() {
    return this.rs.getFile();
  }

  /**
   * Returns a ResultSet set on the wished column.
   *
   * @param column Column.
   * @return ResultSet describing to wished column?
   * @throws SQLIllegalColumnIndexException if the column index is out of bounds.
   * @throws SQLConnectionClosedException if the underlying connection is closed.
   */
  private DBFBuiltInMemoryResultSetForColumnsListing desc(int column)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    DBFBuiltInMemoryResultSetForColumnsListing rsDatabase =
        (DBFBuiltInMemoryResultSetForColumnsListing)
            this.metadata.getColumns(null, null, null, null);

    if (column > getColumnCount()) {
      rsDatabase.close();
      String message =
          format(Level.WARNING, "excp.illegal_column_index_metadata", column, getColumnCount());
      throw new SQLIllegalColumnIndexException(message, this.rs.getSQL(), getFile(), column);
    }

    // TODO Implements ResultSet:absolute(int) instead.
    for (int index = 1; index <= column; index++) {
      try {
        rsDatabase.next();
      } catch (SQLNoResultException e) {
        // We encounter an internal API error in this case.
        rsDatabase.close();
        String message =
            format(
                Level.SEVERE,
                "assert.less_column_in_metadata_than_expected",
                column,
                getColumnCount());
        throw new RuntimeException(message, e);
      }
    }

    return rsDatabase;
  }
}
