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

package org.apache.sis.internal.shapefile.jdbc.resultset;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.SQLConnectionClosedException;
import org.apache.sis.internal.shapefile.jdbc.connection.DBFConnection;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * Common implemented features of all ResultSets : those based on a record, but also those returning
 * results forged in memory.
 *
 * @author Marc LE BIHAN
 */
public abstract class DBFResultSet extends AbstractResultSet {
  /** Indicates if the ResultSet is closed. */
  protected boolean isClosed;

  /** SQL Statement. */
  protected String sql;

  /** true, if the last column had the SQL NULL value (for the ResultSet.wasNull() method). */
  protected boolean wasNull;

  /** Parent statement. */
  protected DBFStatement statement;

  /**
   * Constructs a result set.
   *
   * @param stmt Parent statement.
   * @param sqlQuery SQL Statment that produced this ResultSet.
   */
  public DBFResultSet(final DBFStatement stmt, String sqlQuery) {
    Objects.requireNonNull(stmt, "the statement referred by the ResultSet cannot be null.");

    this.statement = stmt;
    this.sql = sqlQuery;
  }

  /** Defaults to {@link #last()} followed by {@link #next()}. */
  @Override
  public void afterLast() throws SQLException {
    if (last()) next();
  }

  /**
   * Asserts that the connection, statement and result set are together opened.
   *
   * @throws SQLConnectionClosedException if one of them is closed.
   */
  protected void assertNotClosed() throws SQLConnectionClosedException {
    this.statement.assertNotClosed();

    if (this.isClosed) {
      throw new SQLConnectionClosedException(
          format(Level.WARNING, "excp.closed_resultset", this.sql, getFile().getName()),
          this.sql,
          getFile());
    }
  }

  /** Defaults to {@link #absolute(int)}. */
  @Override
  public void beforeFirst() throws SQLException {
    absolute(0);
  }

  /** @see java.sql.ResultSet#close() */
  @Override
  public void close() {
    if (isClosed()) return;

    this.statement.notifyCloseResultSet(this);
    this.isClosed = true;
  }

  /**
   * Returns the column index for the given column name. The default implementation of all methods
   * expecting a column label will invoke this method.
   *
   * @param columnLabel The name of the column.
   * @return The index of the given column name : first column is 1.
   * @throws SQLNoSuchFieldException if there is no field with this name in the query.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @Override
  @SuppressWarnings("resource") // The connection is only used to get the column index.
  public int findColumn(String columnLabel)
      throws SQLNoSuchFieldException, SQLConnectionClosedException {
    DBFConnection cnt = (DBFConnection) this.statement.getConnection();
    return cnt.findColumn(columnLabel, getSQL());
  }

  /** Defaults to {@link #absolute(int)}. */
  @Override
  public boolean first() throws SQLException {
    return absolute(1);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Array getArray(String columnLabel) throws SQLException {
    return getArray(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public InputStream getAsciiStream(String columnLabel) throws SQLException {
    return getAsciiStream(findColumn(columnLabel));
  }

  /**
   * @deprecated Replaced by {@link #getBigDecimal(int)}. Defaults to {@link #getBigDecimal(int)}
   *     followed by {@link BigDecimal#setScale(int)}.
   */
  @Override
  @Deprecated
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    final BigDecimal d = getBigDecimal(columnIndex);
    return (d != null) ? d.setScale(scale) : null;
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public InputStream getBinaryStream(String columnLabel) throws SQLException {
    return getBinaryStream(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Blob getBlob(String columnLabel) throws SQLException {
    return getBlob(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Reader getCharacterStream(String columnLabel) throws SQLException {
    return getCharacterStream(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Clob getClob(String columnLabel) throws SQLException {
    return getClob(findColumn(columnLabel));
  }

  /** Defaults to {@link Statement#getResultSetConcurrency()}. */
  @Override
  public int getConcurrency() throws SQLException {
    return getStatement().getResultSetConcurrency();
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
    return getDate(findColumn(columnLabel), cal);
  }

  /** Defaults to {@link Statement#getFetchDirection()}. */
  @Override
  public int getFetchDirection() throws SQLException {
    return getStatement().getFetchDirection();
  }

  /** @see java.sql.ResultSet#getFetchSize() */
  @Override
  public int getFetchSize() throws SQLException {
    return getStatement().getFetchSize();
  }

  /**
   * Return a field name.
   *
   * @param columnIndex Column index.
   * @param sqlStatement For information, the SQL statement that is attempted.
   * @return Field Name.
   * @throws SQLIllegalColumnIndexException if the index is out of bounds.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  @SuppressWarnings("resource") // Only use the current connection to get the field name.
  public String getFieldName(int columnIndex, String sqlStatement)
      throws SQLIllegalColumnIndexException, SQLConnectionClosedException {
    DBFConnection cnt = (DBFConnection) this.statement.getConnection();
    return cnt.getFieldName(columnIndex, sqlStatement);
  }

  /**
   * Returns the Database File.
   *
   * @return Database File.
   */
  @Override
  public File getFile() {
    return this.statement.getFile();
  }

  /** Defaults to {@link Statement#getResultSetHoldability()}. */
  @Override
  public int getHoldability() throws SQLException {
    return getStatement().getResultSetHoldability();
  }

  /**
   * Returns the JDBC interface implemented by this class. This is used for formatting error
   * messages.
   */
  @Override
  public Class<?> getInterface() {
    return ResultSet.class;
  }

  /**
   * Defaults to {@link #getCharacterStream(int)} on the assumption that the fact that Java use
   * UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    return getCharacterStream(columnIndex);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    return getNCharacterStream(findColumn(columnLabel));
  }

  /**
   * Defaults to {@link #getString(int)} on the assumption that the fact that Java use UTF-16
   * internally makes the two methods identical in behavior.
   */
  @Override
  public String getNString(int columnIndex) throws SQLException {
    return getString(columnIndex);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public String getNString(String columnLabel) throws SQLException {
    return getNString(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public RowId getRowId(String columnLabel) throws SQLException {
    return getRowId(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
    return getTime(findColumn(columnLabel), cal);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Timestamp getTimestamp(String columnLabel) throws SQLException {
    return getTimestamp(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
    return getTimestamp(findColumn(columnLabel), cal);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public URL getURL(String columnLabel) throws SQLException {
    return getURL(findColumn(columnLabel));
  }

  /** @see java.sql.Wrapper#isWrapperFor(java.lang.Class) */
  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return iface.isAssignableFrom(getInterface());
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Object getObject(String columnLabel) throws SQLException {
    return getObject(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
    return getObject(findColumn(columnLabel), map);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    return getObject(findColumn(columnLabel), type);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public Ref getRef(String columnLabel) throws SQLException {
    return getRef(findColumn(columnLabel));
  }

  /**
   * Returns the SQL query that created that ResultSet.
   *
   * @return SQL query.
   */
  public String getSQL() {
    return this.sql;
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    return getSQLXML(findColumn(columnLabel));
  }

  /**
   * Returns the parent statement.
   *
   * @throws SQLConnectionClosedException if the statement is closed.
   */
  @Override
  public Statement getStatement() throws SQLConnectionClosedException {
    assertNotClosed();
    return this.statement;
  }

  /** Defaults to {@link Statement#getResultSetType()}. */
  @Override
  public int getType() throws SQLException {
    return getStatement().getResultSetType();
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  @Deprecated
  public InputStream getUnicodeStream(String columnLabel) throws SQLException {
    return getUnicodeStream(findColumn(columnLabel));
  }

  /** @see java.sql.ResultSet#isBeforeFirst() */
  @Override
  public boolean isBeforeFirst() throws SQLException {
    return getRow() == 0;
  }

  /**
   * Returns {@code true} if this result set has been closed.
   *
   * @return true if the database is closed.
   */
  @Override
  public boolean isClosed() {
    return this.isClosed || this.statement.isClosed();
  }

  /** @see java.sql.ResultSet#isFirst() */
  @Override
  public boolean isFirst() throws SQLException {
    return getRow() == 1;
  }

  /** Defaults to {@link #absolute(int)}. */
  @Override
  public boolean last() throws SQLException {
    return absolute(-1);
  }

  /** Defaults to {@link #relative(int)}. */
  @Override
  public boolean previous() throws SQLException {
    return relative(-1);
  }

  /** Defaults to {@link #absolute(int)} with an offset computed from {@link #getRow()}. */
  @Override
  public boolean relative(int rows) throws SQLException {
    return absolute(rows - getRow());
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateArray(String columnLabel, Array x) throws SQLException {
    updateArray(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    updateAsciiStream(findColumn(columnLabel), x);
  }

  /** Delegates to {@link #updateAsciiStream(int, InputStream, long)} */
  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    updateAsciiStream(columnIndex, x, (long) length);
  }

  /** Delegates to {@link #updateAsciiStream(String, InputStream, long)} */
  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
    updateAsciiStream(columnLabel, x, (long) length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    updateAsciiStream(findColumn(columnLabel), x, length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
    updateBigDecimal(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    updateBinaryStream(findColumn(columnLabel), x);
  }

  /** Delegates to {@link #updateBinaryStream(int, InputStream, long)}. */
  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    updateBinaryStream(columnIndex, x, (long) length);
  }

  /** Delegates to {@link #updateBinaryStream(String, InputStream, long)}. */
  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length)
      throws SQLException {
    updateBinaryStream(columnLabel, x, (long) length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length)
      throws SQLException {
    updateBinaryStream(findColumn(columnLabel), x, length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBlob(String columnLabel, Blob x) throws SQLException {
    updateBlob(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBlob(String columnLabel, InputStream x, long length) throws SQLException {
    updateBlob(findColumn(columnLabel), x, length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBlob(String columnLabel, InputStream x) throws SQLException {
    updateBlob(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    updateBoolean(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateByte(String columnLabel, byte x) throws SQLException {
    updateByte(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    updateBytes(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateCharacterStream(String columnLabel, Reader x) throws SQLException {
    updateCharacterStream(findColumn(columnLabel), x);
  }

  /** Delegates to {@link #updateCharacterStream(int, Reader, long)} */
  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    updateCharacterStream(columnIndex, x, (long) length);
  }

  /** Delegates to {@link #updateCharacterStream(String, Reader, long)} */
  @Override
  public void updateCharacterStream(String columnLabel, Reader x, int length) throws SQLException {
    updateCharacterStream(columnLabel, x, (long) length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateClob(String columnLabel, Clob x) throws SQLException {
    updateClob(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateClob(String columnLabel, Reader x, long length) throws SQLException {
    updateClob(findColumn(columnLabel), x, length);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateClob(String columnLabel, Reader x) throws SQLException {
    updateClob(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateCharacterStream(String columnLabel, Reader x, long length) throws SQLException {
    updateCharacterStream(findColumn(columnLabel), x, length);
  }

  /**
   * Defaults to {@link #updateCharacterStream(int, Reader)} on the assumption that the fact that
   * Java use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    updateCharacterStream(columnIndex, x);
  }

  /**
   * Defaults to {@link #updateCharacterStream(String, Reader)} on the assumption that the fact that
   * Java use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNCharacterStream(String columnLabel, Reader x) throws SQLException {
    updateCharacterStream(columnLabel, x);
  }

  /**
   * Defaults to {@link #updateCharacterStream(int, Reader, int)} on the assumption that the fact
   * that Java use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    updateCharacterStream(columnIndex, x, length);
  }

  /**
   * Defaults to {@link #updateCharacterStream(String, Reader, int)} on the assumption that the fact
   * that Java use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNCharacterStream(String columnLabel, Reader x, long length)
      throws SQLException {
    updateCharacterStream(columnLabel, x, length);
  }
  /**
   * Defaults to {@link #updateClob(int, Clob)} on the assumption that the fact that Java use UTF-16
   * internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNClob(int columnIndex, NClob x) throws SQLException {
    updateClob(columnIndex, x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateNClob(String columnLabel, NClob x) throws SQLException {
    updateNClob(findColumn(columnLabel), x);
  }

  /**
   * Defaults to {@link #updateClob(int, Reader, long)} on the assumption that the fact that Java
   * use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNClob(int columnIndex, Reader x, long length) throws SQLException {
    updateClob(columnIndex, x, length);
  }

  /**
   * Defaults to {@link #updateClob(String, Reader, long)} on the assumption that the fact that Java
   * use UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    updateClob(columnLabel, reader, length);
  }

  /**
   * Defaults to {@link #updateClob(int, Reader)} on the assumption that the fact that Java use
   * UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    updateClob(columnIndex, reader);
  }

  /**
   * Defaults to {@link #updateClob(String, Reader)} on the assumption that the fact that Java use
   * UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    updateClob(columnLabel, reader);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateDate(String columnLabel, Date x) throws SQLException {
    updateDate(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateDouble(String columnLabel, double x) throws SQLException {
    updateDouble(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateFloat(String columnLabel, float x) throws SQLException {
    updateFloat(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateInt(String columnLabel, int x) throws SQLException {
    updateInt(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateLong(String columnLabel, long x) throws SQLException {
    updateLong(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateNull(String columnLabel) throws SQLException {
    updateNull(findColumn(columnLabel));
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateObject(String columnLabel, Object x) throws SQLException {
    updateObject(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
    updateObject(findColumn(columnLabel), x, scaleOrLength);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateRef(String columnLabel, Ref x) throws SQLException {
    updateRef(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    updateRowId(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateShort(String columnLabel, short x) throws SQLException {
    updateShort(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateSQLXML(String columnLabel, SQLXML x) throws SQLException {
    updateSQLXML(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateString(String columnLabel, String x) throws SQLException {
    updateString(findColumn(columnLabel), x);
  }

  /**
   * Defaults to {@link #updateString(int, String)} on the assumption that the fact that Java use
   * UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNString(int columnIndex, String nString) throws SQLException {
    updateString(columnIndex, nString);
  }

  /**
   * Defaults to {@link #updateString(String, String)} on the assumption that the fact that Java use
   * UTF-16 internally makes the two methods identical in behavior.
   */
  @Override
  public void updateNString(String columnLabel, String nString) throws SQLException {
    updateString(columnLabel, nString);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateTime(String columnLabel, Time x) throws SQLException {
    updateTime(findColumn(columnLabel), x);
  }

  /**
   * Defaults to the index-based version of this method. The given column name is mapped to a column
   * index by {@link #findColumn(String)}.
   */
  @Override
  public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
    updateTimestamp(findColumn(columnLabel), x);
  }

  /** @see java.sql.ResultSet#wasNull() */
  @Override
  public boolean wasNull() {
    return this.wasNull;
  }

  /**
   * Get a field description.
   *
   * @param columnLabel Column label.
   * @param sqlStatement SQL Statement.
   * @return ResultSet with current row set on the wished field.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if no column with that name exists.
   */
  public ResultSet getFieldDesc(String columnLabel, String sqlStatement)
      throws SQLConnectionClosedException, SQLNoSuchFieldException {
    return ((DBFConnection) ((DBFStatement) getStatement()).getConnection())
        .getFieldDesc(columnLabel, sqlStatement);
  }

  /**
   * Get a field description.
   *
   * @param column Column index.
   * @param sqlStatement SQL Statement.
   * @return ResultSet with current row set on the wished field.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLIllegalColumnIndexException if the column index is out of bounds.
   */
  public ResultSet getFieldDesc(int column, String sqlStatement)
      throws SQLConnectionClosedException, SQLIllegalColumnIndexException {
    return ((DBFConnection) ((DBFStatement) getStatement()).getConnection())
        .getFieldDesc(column, sqlStatement);
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return format(
        "toString",
        this.statement != null ? this.statement.toString() : null,
        this.sql,
        isClosed() == false);
  }
}
