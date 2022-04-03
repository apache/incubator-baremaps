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

package org.apache.sis.internal.shapefile.jdbc.resultset;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.DBase3FieldDescriptor;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * Special ResultSet listing columns contained in this DBase 3.
 *
 * @author Marc LE BIHAN
 */
public class DBFBuiltInMemoryResultSetForColumnsListing extends BuiltInMemoryResultSet {
  /** Current field descriptor. */
  private DBase3FieldDescriptor current;

  /** Iterator. */
  private Iterator<DBase3FieldDescriptor> itDescriptor;

  /** Column index. */
  private int columnIndex;

  /** Indicates if the ResultSet is set after the last record. */
  private boolean afterLast = false;

  /**
   * Construct a ResultSet.
   *
   * @param stmt Statement.
   * @param fieldsDescriptors Fields descriptors.
   */
  public DBFBuiltInMemoryResultSetForColumnsListing(
      DBFStatement stmt, List<DBase3FieldDescriptor> fieldsDescriptors) {
    super(stmt, "driver list columns in this DBase 3 file");
    this.itDescriptor = fieldsDescriptors.iterator();
  }

  /**
   * @see java.sql.ResultSet#getString(java.lang.String)
   * @throws SQLNoSuchFieldException if the column does not exist.
   */
  @Override
  public String getString(String columnLabel) throws SQLNoSuchFieldException {
    logStep("getString", columnLabel);

    switch (columnLabel) {
        // String => table name
      case "TABLE_NAME":
        {
          String tableName = getTableName();
          this.wasNull = (tableName == null);
          return tableName;
        }

        // String => column name
      case "COLUMN_NAME":
        {
          String columnName = this.current.getName();
          this.wasNull = (columnName == null);
          return columnName;
        }

        // String => Data source dependent type name, for a UDT the type name is fully qualified
      case "TYPE_NAME":
        {
          String typeName = this.current.getType() != null ? toColumnTypeName() : null;
          this.wasNull = (typeName == null);
          return typeName;
        }

        /**
         * Columns responding to features that aren't handled by DBase 3. and return always a
         * default value NULL or "NO"...
         */

        // String => table catalog (may be null)
      case "TABLE_CAT":
        {
          this.wasNull = true;
          return null;
        }

        // String => table schema (may be null)
      case "TABLE_SCHEM":
        {
          this.wasNull = true;
          return null;
        }

        // String => comment describing column (may be null)
      case "REMARKS":
        this.wasNull = true;
        return null;

        // String => default value for the column, which should be interpreted as a string when the
        // value is enclosed in single quotes (may be null)
      case "COLUMN_DEF":
        {
          this.wasNull = true;
          return null;
        }

        // String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE
        // isn't REF)
      case "SCOPE_CATALOG":
        {
          this.wasNull = true;
          return null;
        }

        // String => schema of table that is the scope of a reference attribute (null if the
        // DATA_TYPE isn't REF)
      case "SCOPE_SCHEMA":
        {
          this.wasNull = true;
          return null;
        }

        // String => table name that this the scope of a reference attribute (null if the DATA_TYPE
        // isn't REF)
      case "SCOPE_TABLE":
        {
          this.wasNull = true;
          return null;
        }

        /**
         * String => Indicates whether this column is auto incremented YES --- if the column is auto
         * incremented NO --- if the column is not auto incremented empty string --- if it cannot be
         * determined whether the column is auto incremented
         */
      case "IS_AUTOINCREMENT":
        {
          this.wasNull = false;
          return "NO";
        }

        /**
         * String => Indicates whether this is a generated column YES --- if this a generated column
         * NO --- if this not a generated column empty string --- if it cannot be determined whether
         * this is a generated column
         */
      case "IS_GENERATEDCOLUMN":
        {
          this.wasNull = false;
          return "NO";
        }

      default:
        {
          // Attempt to load it from an Integer column and convert it.
          int value = getInt(columnLabel);
          return MessageFormat.format("{0,number,#0}", value); // Remove decimal separators.
        }
    }
  }

  /**
   * @see java.sql.ResultSet#getInt(java.lang.String)
   * @throws SQLNoSuchFieldException if the column does not exist.
   */
  @Override
  public int getInt(String columnLabel) throws SQLNoSuchFieldException {
    logStep("getInt", columnLabel);

    switch (columnLabel) {
        // int => SQL type from java.sql.Types
      case "DATA_TYPE":
        {
          this.wasNull = false;
          return toSQLDataType();
        }

        // int => column size.
      case "COLUMN_SIZE":
        {
          this.wasNull = false;
          return toPrecision();
        }

        // int => the number of fractional digits. Null is returned for data types where
        // DECIMAL_DIGITS is not applicable.
      case "DECIMAL_DIGITS":
        {
          int scale = toScale();
          this.wasNull = toScale() == -1;
          return scale == -1 ? 0 : scale;
        }

        // int => Radix (typically either 10 or 2)
      case "NUM_PREC_RADIX":
        {
          return 10;
        }

        /**
         * int => is NULL allowed. columnNoNulls - might not allow NULL values columnNullable -
         * definitely allows NULL values columnNullableUnknown - nullability unknown
         */
      case "NULLABLE":
        {
          this.wasNull = false;
          return DatabaseMetaData.columnNullableUnknown;
        }

        // int => unused
      case "SQL_DATA_TYPE":
        {
          this.wasNull = false;
          return toSQLDataType();
        }

        // int => for char types the maximum number of bytes in the column
      case "CHAR_OCTET_LENGTH":
        {
          if (toSQLDataType() == Types.CHAR) {
            return toPrecision();
          }

          return 0;
        }

        // int => index of column in table (starting at 1)
      case "ORDINAL_POSITION":
        {
          return this.columnIndex;
        }

        /**
         * Columns responding to features that aren't handled by DBase 3. and return always a
         * default value NULL or "NO"...
         */

        // short => source type of a distinct type or user-generated Ref type, SQL type from
        // java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
      case "SOURCE_DATA_TYPE":
        {
          this.wasNull = true;
          return 0;
        }

        // is not used.
      case "BUFFER_LENGTH":
        {
          this.wasNull = false;
          return 0;
        }

        // int => unused
      case "SQL_DATETIME_SUB":
        {
          this.wasNull = false;
          return 0;
        }

      default:
        // FIXME : this function is not perfect. It a column label is given that refers to a field
        // described in getString(..) this function
        // will tell that the field doesn't exist. It's not true : the field is not numeric. But as
        // getString(..) defaults to getInt(...),
        // getInt(..) cannot default to getString(..), else the function will run in a cycle.
        String message = format(Level.WARNING, "excp.no_desc_field", columnLabel, getTableName());
        throw new SQLNoSuchFieldException(message, "asking columns desc", getFile(), columnLabel);
    }
  }

  /** @see java.sql.ResultSet#next() */
  @Override
  public boolean next() throws SQLNoResultException {
    if (this.itDescriptor.hasNext()) {
      this.current = this.itDescriptor.next();
      this.columnIndex++;
      return true;
    } else {
      if (this.afterLast) {
        // The ResultSet has no more records and has been call one time too much.
        this.afterLast = true;

        String message = format(Level.WARNING, "excp.no_more_desc", getTableName());
        throw new SQLNoResultException(message, "asking columns desc", getFile());
      } else {
        return false;
      }
    }
  }

  /**
   * Returns the SQL Datatype of the DBase 3 type for the current field.
   *
   * @return SQL Datatype.
   */
  private int toSQLDataType() {
    switch (this.current.getType()) {
      case AutoIncrement:
        return Types.INTEGER;

      case Character:
        return Types.CHAR;

      case Integer:
        return Types.INTEGER;

      case Date:
        return Types.DATE;

      case Double:
        return Types.DOUBLE;

      case FloatingPoint:
        return Types.FLOAT;

      case Number:
        return Types.DECIMAL;

      case Logical:
        return Types.BOOLEAN;

      case Currency:
        return Types.NUMERIC;

      case DateTime:
        return Types.TIMESTAMP; // TODO : I think ?

      case TimeStamp:
        return Types.TIMESTAMP;

      case Memo:
        return Types.BLOB;

      case Picture:
        return Types.BLOB;

      case VariField:
        return Types.OTHER;

      case Variant:
        return Types.OTHER;

      default:
        return Types.OTHER;
    }
  }

  /**
   * Returns the column type name of the current field.
   *
   * @return Column type name.
   */
  private String toColumnTypeName() {
    switch (this.current.getType()) {
      case AutoIncrement:
        return "AUTO_INCREMENT";

      case Character:
        return "CHAR";

      case Integer:
        return "INTEGER";

      case Date:
        return "DATE";

      case Double:
        return "DOUBLE";

      case FloatingPoint:
        return "FLOAT";

      case Number:
        return "DECIMAL";

      case Logical:
        return "BOOLEAN";

      case Currency:
        return "CURRENCY";

      case DateTime:
        return "DATETIME";

      case TimeStamp:
        return "TIMESTAMP";

      case Memo:
        return "MEMO";

      case Picture:
        return "PICTURE";

      case VariField:
        return "VARIFIELD";

      case Variant:
        return "VARIANT";

      default:
        return "UNKNOWN";
    }
  }

  /**
   * Returns the precision of the current field.
   *
   * @return Precision of the current field.
   */
  public int toPrecision() {
    switch (this.current.getType()) {
      case AutoIncrement:
      case Character:
      case Integer:
        return this.current.getLength();

      case Date:
        return 8;

      case Double:
      case FloatingPoint:
      case Number:
        return this.current.getLength();

      case Logical:
        return 0;

      case Currency:
      case DateTime:
      case TimeStamp:
        return this.current.getLength();

      case Memo:
      case Picture:
      case VariField:
      case Variant:
        return 0;

      default:
        return this.current.getLength();
    }
  }

  /**
   * Returns the scale of the current field.
   *
   * @return Scale of the current field, -1 means : this field is not numeric.
   */
  private int toScale() {
    switch (this.current.getType()) {
      case AutoIncrement:
      case Logical:
        return 0;

      case Integer:
      case Double:
      case FloatingPoint:
      case Number:
      case Currency:
        return this.current.getDecimalCount();

      case Character:
      case Date:
      case DateTime:
      case TimeStamp:
      case Memo:
      case Picture:
      case VariField:
      case Variant:
        return -1;

      default:
        return -1;
    }
  }
}
