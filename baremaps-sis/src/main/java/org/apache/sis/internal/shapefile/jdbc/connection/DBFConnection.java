/*
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

package org.apache.sis.internal.shapefile.jdbc.connection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.sis.internal.shapefile.jdbc.*;
import org.apache.sis.internal.shapefile.jdbc.metadata.DBFDatabaseMetaData;
import org.apache.sis.internal.shapefile.jdbc.resultset.*;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * Connection to a DBF database.
 *
 * @author Marc Le Bihan
 * @version 0.5
 * @since 0.5
 * @module
 */
public class DBFConnection extends AbstractConnection {
  /** The object to use for reading the database content. */
  final File databaseFile;

  /** Opened statement. */
  private HashSet<DBFStatement> openedStatements = new HashSet<>();

  /** ByteReader. */
  private Dbase3ByteReader byteReader;

  /**
   * Constructs a connection to the given database.
   *
   * @param datafile Data file ({@code .dbf} extension).
   * @param br Byte reader to use for reading binary content.
   * @throws SQLDbaseFileNotFoundException if the Database file cannot be found or is not a file.
   */
  public DBFConnection(final File datafile, Dbase3ByteReader br)
      throws SQLDbaseFileNotFoundException {
    // Check that file exists.
    if (!datafile.exists()) {
      throw new SQLDbaseFileNotFoundException(
          format(Level.WARNING, "excp.file_not_found", datafile.getAbsolutePath()));
    }

    // Check that its not a directory.
    if (datafile.isDirectory()) {
      throw new SQLDbaseFileNotFoundException(
          format(Level.WARNING, "excp.directory_not_expected", datafile.getAbsolutePath()));
    }

    this.databaseFile = datafile;
    this.byteReader = br;
    log(
        Level.FINE,
        "log.database_connection_opened",
        this.databaseFile.getAbsolutePath(),
        "FIXME : column desc.");
  }

  /** Closes the connection to the database. */
  @Override
  public void close() {
    if (isClosed()) return;

    try {
      // Check if all the underlying connections that has been opened with this connection has been
      // closed.
      // If not, we log a warning to help the developer.
      if (this.openedStatements.size() > 0) {
        log(
            Level.WARNING,
            "log.statements_left_opened",
            this.openedStatements.size(),
            this.openedStatements.stream()
                .map(DBFStatement::toString)
                .collect(Collectors.joining(", ")));
      }

      this.byteReader.close();
    } catch (IOException e) {
      log(Level.FINE, e.getMessage(), e);
    }
  }

  /**
   * Creates an object for sending SQL statements to the database.
   *
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  public Statement createStatement() throws SQLConnectionClosedException {
    assertNotClosed();

    DBFStatement stmt = new DBFStatement(this);
    this.openedStatements.add(stmt);
    return stmt;
  }

  /** @see java.sql.Connection#getCatalog() */
  @Override
  public String getCatalog() {
    return null; // DBase 3 offers no catalog.
  }

  /**
   * Returns the charset.
   *
   * @return Charset.
   */
  public Charset getCharset() {
    return this.byteReader.getCharset();
  }

  /**
   * Returns the database File.
   *
   * @return File.
   */
  @Override
  public File getFile() {
    return this.databaseFile;
  }

  /**
   * Returns the JDBC interface implemented by this class. This is used for formatting error
   * messages.
   */
  @Override
  protected final Class<?> getInterface() {
    return Connection.class;
  }

  /** @see java.sql.Connection#getMetaData() */
  @Override
  public DatabaseMetaData getMetaData() {
    return new DBFDatabaseMetaData(this);
  }

  /** Returns {@code true} if this connection has been closed. */
  @Override
  public boolean isClosed() {
    return this.byteReader.isClosed();
  }

  /**
   * Returns {@code true} if the connection has not been closed and is still valid. The timeout
   * parameter is ignored and this method bases itself only on {@link #isClosed()} state.
   */
  @Override
  public boolean isValid(@SuppressWarnings("unused") int timeout) {
    return !isClosed();
  }

  /** @see java.sql.Wrapper#isWrapperFor(java.lang.Class) */
  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return iface.isAssignableFrom(getInterface());
  }

  /**
   * Asserts that the connection is opened.
   *
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  public void assertNotClosed() throws SQLConnectionClosedException {
    // If closed throw an exception specifying the name if the DBF that is closed.
    if (isClosed()) {
      throw new SQLConnectionClosedException(
          format(Level.WARNING, "excp.closed_connection", getFile().getName()), null, getFile());
    }
  }

  /**
   * Method called by Statement class to notity this connection that a statement has been closed.
   *
   * @param stmt Statement that has been closed.
   */
  public void notifyCloseStatement(DBFStatement stmt) {
    Objects.requireNonNull(stmt, "The statement notified being closed cannot be null.");

    if (this.openedStatements.remove(stmt) == false) {
      throw new RuntimeException(
          format(Level.SEVERE, "assert.statement_not_opened_by_me", stmt, toString()));
    }
  }

  /**
   * Returns the column index for the given column name. The default implementation of all methods
   * expecting a column label will invoke this method.
   *
   * @param columnLabel The name of the column.
   * @param sql For information, the SQL statement that is attempted.
   * @return The index of the given column name : first column is 1.
   * @throws SQLNoSuchFieldException if there is no field with this name in the query.
   */
  public int findColumn(String columnLabel, String sql) throws SQLNoSuchFieldException {
    return this.byteReader.findColumn(columnLabel, sql);
  }

  /**
   * Returns the column count of the table of the database.
   *
   * @return Column count.
   */
  public int getColumnCount() {
    return this.byteReader.getColumnCount();
  }

  /**
   * Get a field description.
   *
   * @param columnLabel Column label.
   * @param sql SQL Statement.
   * @return ResultSet with current row set on the wished field.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if no column with that name exists.
   */
  public ResultSet getFieldDesc(String columnLabel, String sql)
      throws SQLConnectionClosedException, SQLNoSuchFieldException {
    Objects.requireNonNull(columnLabel, "The column name cannot be null.");

    DBFBuiltInMemoryResultSetForColumnsListing rs =
        (DBFBuiltInMemoryResultSetForColumnsListing)
            ((DBFDatabaseMetaData) getMetaData()).getColumns(null, null, null, null);

    try {
      while (rs.next()) {
        try {
          if (rs.getString("COLUMN_NAME").equalsIgnoreCase(columnLabel)) {
            return rs;
          }
        } catch (SQLNoSuchFieldException e) {
          // if it is the COLUMN_NAME column that has not been found in the desc ResultSet, we have
          // an internal error.
          rs.close();
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    } catch (SQLNoResultException e) {
      // if we run out of bound of the ResultSet, the boolean returned by next() has not been
      // checked well, and it's an internal error.
      rs.close();
      throw new RuntimeException(e.getMessage(), e);
    }

    // But if we are here, we have not found the column with this name, and we have to throw an
    // SQLNoSuchFieldException exception ourselves.
    String message =
        format("excp.no_such_column_in_resultset", columnLabel, sql, getFile().getName());
    throw new SQLNoSuchFieldException(message, sql, getFile(), columnLabel);
  }

  /**
   * Get a field description.
   *
   * @param column Column index.
   * @param sql SQL Statement.
   * @return ResultSet with current row set on the wished field.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLIllegalColumnIndexException if the column index is out of bounds.
   */
  public ResultSet getFieldDesc(int column, String sql)
      throws SQLConnectionClosedException, SQLIllegalColumnIndexException {
    DBFBuiltInMemoryResultSetForColumnsListing rs =
        (DBFBuiltInMemoryResultSetForColumnsListing)
            ((DBFDatabaseMetaData) getMetaData()).getColumns(null, null, null, null);

    if (column <= 0 || column > getColumnCount()) {
      rs.close();
      String message = format("excp.illegal_column_index_metadata", column, getColumnCount());
      throw new SQLIllegalColumnIndexException(message, sql, getFile(), column);
    }

    // TODO Implements ResultSet:absolute(int) instead.
    for (int index = 1; index <= column; index++) {
      try {
        rs.next();
      } catch (SQLNoResultException e) {
        // We encounter an internal API error in this case.
        rs.close();
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    return rs;
  }

  /**
   * Returns the fields descriptors in their binary format.
   *
   * @return Fields descriptors.
   */
  public List<DBase3FieldDescriptor> getFieldsDescriptors() {
    return this.byteReader.getFieldsDescriptors();
  }

  /**
   * Return a field name.
   *
   * @param columnIndex Column index.
   * @param sql For information, the SQL statement that is attempted.
   * @return Field Name.
   * @throws SQLIllegalColumnIndexException if the index is out of bounds.
   */
  public String getFieldName(int columnIndex, String sql) throws SQLIllegalColumnIndexException {
    return this.byteReader.getFieldName(columnIndex, sql);
  }

  /**
   * Checks if a next row is available. Warning : it may be a deleted one.
   *
   * @return true if a next row is available.
   */
  public boolean nextRowAvailable() {
    return this.byteReader.nextRowAvailable();
  }

  /**
   * Read the next row as a set of objects.
   *
   * @return Map of field name / object value, or null if EoF has been encountered.
   */
  public Map<String, byte[]> readNextRowAsObjects() {
    return this.byteReader.readNextRowAsObjects();
  }

  /**
   * Returns the record number of the last record red.
   *
   * @return The record number.
   */
  public int getRowNum() {
    return this.byteReader.getRowNum();
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return format("toString", this.databaseFile.getAbsolutePath(), isClosed() == false);
  }
}
