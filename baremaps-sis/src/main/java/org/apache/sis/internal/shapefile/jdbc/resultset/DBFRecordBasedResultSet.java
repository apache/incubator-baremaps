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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.SQLConnectionClosedException;
import org.apache.sis.internal.shapefile.jdbc.connection.DBFConnection;
import org.apache.sis.internal.shapefile.jdbc.metadata.DBFResultSetMataData;
import org.apache.sis.internal.shapefile.jdbc.sql.*;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * A ResultSet based on a record.
 *
 * @author Marc LE BIHAN
 */
public class DBFRecordBasedResultSet extends DBFResultSet {
  /** The current record. */
  private Map<String, byte[]> record;

  /** Condition of where clause (currently, only one is handled). */
  private ConditionalClauseResolver singleConditionOfWhereClause;

  /**
   * Indicates that the last result set record matching conditions has already been returned, and a
   * further call of next() shall throw a "no more record" exception.
   */
  private boolean lastResultSetRecordAlreadyReturned;

  /** The record number of this record. */
  private int recordNumber;

  /**
   * Constructs a result set.
   *
   * @param stmt Parent statement.
   * @param sqlQuery SQL Statment that produced this ResultSet.
   * @throws SQLInvalidStatementException if the SQL Statement is invalid.
   */
  public DBFRecordBasedResultSet(final DBFStatement stmt, String sqlQuery)
      throws SQLInvalidStatementException {
    super(stmt, sqlQuery);
    this.singleConditionOfWhereClause = new CrudeSQLParser(this).parse();
  }

  /**
   * @see
   *     org.apache.sis.internal.shapefile.jdbc.resultset.AbstractResultSet#getBigDecimal(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Override
  public BigDecimal getBigDecimal(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getBigDecimal", columnLabel);

    assertNotClosed();

    // Act as if we were a double, but store the result in a pre-created BigDecimal at the end.
    try (DBFBuiltInMemoryResultSetForColumnsListing field =
        (DBFBuiltInMemoryResultSetForColumnsListing) getFieldDesc(columnLabel, sql)) {
      MathContext mc = new MathContext(field.getInt("DECIMAL_DIGITS"), RoundingMode.HALF_EVEN);
      Double doubleValue = getDouble(columnLabel);

      if (doubleValue != null) {
        BigDecimal number = new BigDecimal(doubleValue, mc);
        this.wasNull = false;
        return number;
      } else {
        this.wasNull = true;
        return null;
      }
    }
  }

  /**
   * @see java.sql.ResultSet#getBigDecimal(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public BigDecimal getBigDecimal(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getBigDecimal", columnIndex);
    return getBigDecimal(getFieldName(columnIndex, this.sql));
  }

  /**
   * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
   * @deprecated Deprecated API (from ResultSet Interface)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Deprecated
  @Override
  public BigDecimal getBigDecimal(String columnLabel, int scale)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getBigDecimal", columnLabel, scale);
    assertNotClosed();

    // Act as if we were a double, but store the result in a pre-created BigDecimal at the end.
    MathContext mc = new MathContext(scale, RoundingMode.HALF_EVEN);
    Double doubleValue = getDouble(columnLabel);

    if (doubleValue != null) {
      BigDecimal number = new BigDecimal(getDouble(columnLabel), mc);
      this.wasNull = false;
      return number;
    } else {
      this.wasNull = true;
      return null;
    }
  }

  /**
   * @see java.sql.ResultSet#getDate(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotDateException if the field is not a date.
   */
  @Override
  public Date getDate(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotDateException {
    logStep("getDate", columnLabel);
    assertNotClosed();

    String value = getString(columnLabel);

    if (value == null
        || value.equals(
            "00000000")) { // "00000000" is stored in Database to represent a null value too.
      this.wasNull = true;
      return null; // The ResultSet:getDate() contract is to return null when a null date is
      // encountered.
    } else {
      this.wasNull = false;
    }

    // The DBase 3 date format is "YYYYMMDD".
    // if the length of the string isn't eight characters, the field format is incorrect.
    if (value.length() != 8) {
      String message =
          format(Level.WARNING, "excp.field_is_not_a_date", columnLabel, this.sql, value);
      throw new SQLNotDateException(message, this.sql, getFile(), columnLabel, value);
    }

    // Extract the date parts.
    int year, month, dayOfMonth;

    try {
      year = Integer.parseInt(value.substring(0, 4));
      month = Integer.parseInt(value.substring(5, 7));
      dayOfMonth = Integer.parseInt(value.substring(7));
    } catch (NumberFormatException e) {
      String message =
          format(Level.WARNING, "excp.field_is_not_a_date", columnLabel, this.sql, value);
      throw new SQLNotDateException(message, this.sql, getFile(), columnLabel, value);
    }

    // Create a date.
    Calendar calendar = new GregorianCalendar(year, month - 1, dayOfMonth, 0, 0, 0);
    Date sqlDate = new Date(calendar.getTimeInMillis());
    return sqlDate;
  }

  /**
   * @see java.sql.ResultSet#getDate(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotDateException if the field is not a date.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public Date getDate(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotDateException,
          SQLIllegalColumnIndexException {
    logStep("getDate", columnIndex);
    return getDate(getFieldName(columnIndex, this.sql));
  }

  /**
   * @see java.sql.ResultSet#getDouble(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Override
  public double getDouble(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getDouble", columnLabel);

    Double value = getNumeric(columnLabel, Double::parseDouble);
    this.wasNull = (value == null);
    return value != null
        ? value
        : 0.0; // The ResultSet contract for numbers is to return 0 when a null value is
    // encountered.
  }

  /**
   * @see java.sql.ResultSet#getDouble(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public double getDouble(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getDouble", columnIndex);
    return getDouble(getFieldName(columnIndex, this.sql));
  }

  /**
   * @see java.sql.ResultSet#getFloat(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Override
  public float getFloat(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getFloat", columnLabel);

    Float value = getNumeric(columnLabel, Float::parseFloat);
    this.wasNull = (value == null);
    return value != null
        ? value
        : 0; // The ResultSet contract for numbers is to return 0 when a null value is encountered.
  }

  /**
   * @see java.sql.ResultSet#getFloat(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public float getFloat(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getFloat", columnIndex);
    return getFloat(getFieldName(columnIndex, this.sql));
  }

  /**
   * @see
   *     org.apache.sis.internal.shapefile.jdbc.resultset.AbstractResultSet#getInt(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Override
  public int getInt(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getInt", columnLabel);

    Integer value = getNumeric(columnLabel, Integer::parseInt);
    this.wasNull = (value == null);
    return value != null
        ? value
        : 0; // The ResultSet contract for numbers is to return 0 when a null value is encountered.
  }

  /**
   * @see java.sql.ResultSet#getInt(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public int getInt(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getInt", columnIndex);
    return getInt(getFieldName(columnIndex, this.sql));
  }

  /**
   * @see java.sql.ResultSet#getLong(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   */
  @Override
  public long getLong(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getLong", columnLabel);

    Long value = getNumeric(columnLabel, Long::parseLong);
    this.wasNull = (value == null);
    return value != null
        ? value
        : 0; // The ResultSet contract for numbers is to return 0 when a null value is encountered.
  }

  /**
   * @see java.sql.ResultSet#getLong(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public long getLong(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getLong", columnIndex);
    return getLong(getFieldName(columnIndex, this.sql));
  }

  /** @see java.sql.ResultSet#getMetaData() */
  @Override
  public ResultSetMetaData getMetaData() {
    logStep("getMetaData");

    DBFResultSetMataData meta = new DBFResultSetMataData(this);
    return meta;
  }

  /** @see org.apache.sis.internal.shapefile.jdbc.resultset.AbstractResultSet#getObject(int) */
  @Override
  public Object getObject(int column)
      throws SQLConnectionClosedException, SQLIllegalColumnIndexException,
          SQLFeatureNotSupportedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLNotDateException {
    try (DBFBuiltInMemoryResultSetForColumnsListing field =
        (DBFBuiltInMemoryResultSetForColumnsListing) getFieldDesc(column, this.sql)) {
      String fieldType;

      try {
        fieldType = field.getString("TYPE_NAME");
      } catch (SQLNoSuchFieldException e) {
        // This is an internal trouble because the field type must be found.
        throw new RuntimeException(e.getMessage(), e);
      }

      switch (fieldType) {
        case "AUTO_INCREMENT":
        case "INTEGER":
          return getInt(column);

        case "CHAR":
          return getString(column);

        case "DATE":
          return getDate(column);

        case "DECIMAL":
          {
            // Choose Integer or Long type, if no decimal and that the field is not to big.
            if (field.getInt("DECIMAL_DIGITS") == 0 && field.getInt("COLUMN_SIZE") <= 18) {
              if (field.getInt("COLUMN_SIZE") <= 9) return getInt(column);
              else return getLong(column);
            }

            return getDouble(column);
          }

        case "DOUBLE":
        case "CURRENCY":
          return getDouble(column);

        case "FLOAT":
          return getFloat(column);

        case "BOOLEAN":
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on Boolean");

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
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on " + fieldType);

        default:
          throw unsupportedOperation("ResultSetMetaData.getColumnClassName(..) on " + fieldType);
      }
    }
  }

  /**
   * @see org.apache.sis.internal.shapefile.jdbc.resultset.DBFResultSet#getObject(java.lang.String)
   */
  @Override
  public Object getObject(String columnLabel)
      throws SQLConnectionClosedException, SQLFeatureNotSupportedException, SQLNoSuchFieldException,
          SQLNotNumericException, SQLNotDateException {
    int index = -1;

    try {
      index = findColumn(columnLabel);
      return getObject(index);
    } catch (SQLIllegalColumnIndexException e) {
      String message =
          format(Level.SEVERE, "assert.wrong_index_for_column_name", index, columnLabel);
      throw new RuntimeException(message, e);
    }
  }

  /**
   * Return the record number of this record.
   *
   * @return Record number of this record.
   */
  public int getRowNum() {
    return this.recordNumber;
  }

  /**
   * @see java.sql.ResultSet#getShort(java.lang.String)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric or has a NULL value.
   */
  @Override
  public short getShort(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    logStep("getShort", columnLabel);

    Short value = getNumeric(columnLabel, Short::parseShort);
    this.wasNull = (value == null);
    return value != null
        ? value
        : 0; // The ResultSet contract for numbers is to return 0 when a null value is encountered.
  }

  /**
   * @see java.sql.ResultSet#getShort(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric or has a NULL value.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public short getShort(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLIllegalColumnIndexException {
    logStep("getShort", columnIndex);
    return getShort(getFieldName(columnIndex, this.sql));
  }

  /**
   * Returns the value in the current row for the given column.
   *
   * @param columnLabel Column name.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field does not exist.
   */
  @Override
  @SuppressWarnings("resource") // Only read the current connection to get the Charset.
  public String getString(String columnLabel)
      throws SQLConnectionClosedException, SQLNoSuchFieldException {
    logStep("getString", columnLabel);
    assertNotClosed();

    getFieldDesc(
        columnLabel,
        this.sql); // Ensure that the field queried exists, else a null value here can be
    // interpreted as "not existing" or "has a null value".
    byte[] bytes = this.record.get(columnLabel);

    if (bytes == null) {
      this.wasNull = true;
      return null;
    } else {
      this.wasNull = false;
    }

    // If a non null value has been readed, convert it to the wished Charset (provided one has been
    // given).
    DBFConnection cnt = (DBFConnection) ((DBFStatement) getStatement()).getConnection();
    Charset charset = cnt.getCharset();

    if (charset == null) {
      return new String(bytes);
    } else {
      String withDatabaseCharset = new String(bytes, charset);
      log(Level.FINER, "log.string_field_charset", columnLabel, withDatabaseCharset, charset);
      return withDatabaseCharset;
    }
  }

  /**
   * @see java.sql.ResultSet#getString(int)
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLIllegalColumnIndexException if the column index has an illegal value.
   */
  @Override
  public String getString(int columnIndex)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLIllegalColumnIndexException {
    logStep("getString", columnIndex);
    return (getString(getFieldName(columnIndex, this.sql)));
  }

  /**
   * Moves the cursor forward one row from its current position.
   *
   * @throws SQLInvalidStatementException if the SQL statement is invalid.
   * @throws SQLIllegalParameterException if the value of one parameter of a condition is invalid.
   * @throws SQLNoSuchFieldException if a field mentionned in the condition doesn't exist.
   * @throws SQLUnsupportedParsingFeatureException if the caller asked for a not yet supported
   *     feature of the driver.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNotNumericException if a value or data expected to be numeric isn't.
   * @throws SQLNotDateException if a value or data expected to be a date isn't.
   */
  @Override
  @SuppressWarnings(
      "resource") // Only read the current connection to find if a next row is available and read
  // it.
  public boolean next()
      throws SQLNoResultException, SQLConnectionClosedException, SQLInvalidStatementException,
          SQLIllegalParameterException, SQLNoSuchFieldException,
          SQLUnsupportedParsingFeatureException, SQLNotNumericException, SQLNotDateException {
    logStep("next");
    assertNotClosed();

    DBFConnection cnt = (DBFConnection) ((DBFStatement) getStatement()).getConnection();

    // Check that we aren't at the end of the Database file.
    if (cnt.nextRowAvailable() == false) {
      if (this.lastResultSetRecordAlreadyReturned) {
        throw new SQLNoResultException(
            format(Level.WARNING, "excp.no_more_results", this.sql, getFile().getName()),
            this.sql,
            getFile());
      } else {
        this.lastResultSetRecordAlreadyReturned = true;
        return false;
      }
    }

    return nextRecordMatchingConditions();
  }

  /**
   * Find the next record that match the where condition.
   *
   * @return true if a record has been found.
   * @throws SQLInvalidStatementException if the SQL statement is invalid.
   * @throws SQLIllegalParameterException if the value of one parameter of a condition is invalid.
   * @throws SQLNoSuchFieldException if a field mentionned in the condition doesn't exist.
   * @throws SQLUnsupportedParsingFeatureException if the caller asked for a not yet supported
   *     feature of the driver.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNotNumericException if a value or data expected to be numeric isn't.
   * @throws SQLNotDateException if a value or data expected to be a date isn't.
   */
  @SuppressWarnings(
      "resource") // Only read the current connection to find if a next row is available and read
  // it.
  private boolean nextRecordMatchingConditions()
      throws SQLInvalidStatementException, SQLIllegalParameterException, SQLNoSuchFieldException,
          SQLUnsupportedParsingFeatureException, SQLConnectionClosedException,
          SQLNotNumericException, SQLNotDateException {
    boolean recordMatchesConditions = false;
    DBFConnection cnt = (DBFConnection) ((DBFStatement) getStatement()).getConnection();

    while (cnt.nextRowAvailable() && recordMatchesConditions == false) {
      this.record = cnt.readNextRowAsObjects();
      this.recordNumber = cnt.getRowNum();
      recordMatchesConditions =
          this.singleConditionOfWhereClause == null
              || this.singleConditionOfWhereClause.isVerified(this);
    }

    return recordMatchesConditions;
  }

  /** @see java.sql.Wrapper#isWrapperFor(java.lang.Class) */
  @Override
  public boolean isWrapperFor(Class<?> iface) {
    logStep("isWrapperFor", iface);
    return iface.isAssignableFrom(getInterface());
  }

  /** @see java.sql.ResultSet#wasNull() */
  @Override
  public boolean wasNull() {
    logStep("wasNull");
    return this.wasNull;
  }

  /**
   * Get a numeric value.
   *
   * @param <T> Type of the number.
   * @param columnLabel Column Label.
   * @param parse Parsing function : Integer.parseInt, Float.parseFloat, Long.parseLong, ...
   * @return The expected value or null if null was encountered.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLNoSuchFieldException if the field looked for doesn't exist.
   * @throws SQLNotNumericException if the field value is not numeric or has a NULL value.
   */
  private <T extends Number> T getNumeric(String columnLabel, Function<String, T> parse)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException {
    assertNotClosed();

    try (DBFBuiltInMemoryResultSetForColumnsListing rs =
        (DBFBuiltInMemoryResultSetForColumnsListing) getFieldDesc(columnLabel, this.sql)) {
      String textValue = getString(columnLabel);

      if (textValue == null) {
        return null;
      }

      try {
        textValue = textValue.trim(); // Field must be trimed before being converted.
        T value = parse.apply(textValue);
        return (value);
      } catch (NumberFormatException e) {
        String message =
            format(
                Level.WARNING,
                "excp.field_is_not_numeric",
                columnLabel,
                rs.getString("TYPE_NAME"),
                this.sql,
                textValue);
        throw new SQLNotNumericException(message, this.sql, getFile(), columnLabel, textValue);
      }
    }
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
