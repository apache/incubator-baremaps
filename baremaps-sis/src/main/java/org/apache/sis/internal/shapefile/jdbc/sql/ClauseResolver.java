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

package org.apache.sis.internal.shapefile.jdbc.sql;

import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.AutoChecker;
import org.apache.sis.internal.shapefile.jdbc.SQLConnectionClosedException;
import org.apache.sis.internal.shapefile.jdbc.resultset.*;

/**
 * Base class for clause resolver.
 *
 * @author Marc LE BIHAN
 */
public abstract class ClauseResolver extends AutoChecker {
  /** First comparand. */
  private Object comparand1;

  /** Second comparand. */
  private Object comparand2;

  /** Operator. */
  private String operator;

  /**
   * Construct a where clause resolver.
   *
   * @param cmp1 The first comparand that might be a primitive or a Field.
   * @param cmp2 The second comparand that might be a primitive or a Field.
   * @param op The operator to apply.
   */
  public ClauseResolver(Object cmp1, Object cmp2, String op) {
    this.comparand1 = cmp1;
    this.comparand2 = cmp2;
    this.operator = op;
  }

  /**
   * Returns first comparand.
   *
   * @return First comparand.
   */
  public Object getComparand1() {
    return this.comparand1;
  }

  /**
   * Returns second comparand.
   *
   * @return Second comparand.
   */
  public Object getComparand2() {
    return this.comparand2;
  }

  /**
   * Returns operator.
   *
   * @return Operator.
   */
  public String getOperator() {
    return this.operator;
  }

  /**
   * Set the first comparand.
   *
   * @param comparand First comparand.
   */
  public void setComparand1(Object comparand) {
    this.comparand1 = comparand;
  }

  /**
   * Set the second comparand.
   *
   * @param comparand Second comparand.
   */
  public void setComparand2(Object comparand) {
    this.comparand2 = comparand;
  }

  /**
   * Set the operator.
   *
   * @param op Operator.
   */
  public void setOperator(String op) {
    this.operator = op;
  }

  /**
   * Check if a condition is verified.
   *
   * @param rs ResultSet where the value shall be taken.
   * @return true if the current record of the ResultSet matches this condition.
   * @throws SQLInvalidStatementException if the operator is not valid.
   * @throws SQLIllegalParameterException if a parameter has a value that is not parsable.
   * @throws SQLNoSuchFieldException if a field name doesn't exist in the query.
   * @throws SQLUnsupportedParsingFeatureException if our implementation of the driver still not
   *     handle this data type.
   * @throws SQLNotDateException if a field announced being a date isn't.
   * @throws SQLUnsupportedParsingFeatureException if the driver encounter a type it cannot handle.
   * @throws SQLNotNumericException if a field doesn't carry a numeric value when expected to.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  public boolean isVerified(DBFRecordBasedResultSet rs)
      throws SQLInvalidStatementException, SQLIllegalParameterException, SQLNoSuchFieldException,
          SQLUnsupportedParsingFeatureException, SQLConnectionClosedException,
          SQLNotNumericException, SQLNotDateException {
    switch (getOperator()) {
      case "=":
        return compare(rs) == 0;

      case ">":
        return compare(rs) > 0;

      case ">=":
        return compare(rs) >= 0;

      case "<":
        return compare(rs) < 0;

      case "<=":
        return compare(rs) <= 0;

      default:
        String message =
            format(Level.WARNING, "excp.invalid_statement_operator", getOperator(), rs.getSQL());
        throw new SQLInvalidStatementException(message, rs.getSQL(), rs.getFile());
    }
  }

  /**
   * Returns true if this condition is verified.
   *
   * @param rs The record containing the values to extract, if needed.
   * @return true if it is the case.
   * @throws SQLIllegalParameterException if a parameter has a value that is not parsable.
   * @throws SQLNoSuchFieldException if a field name doesn't exist in the query.
   * @throws SQLUnsupportedParsingFeatureException if our implementation of the driver still not
   *     handle this data type.
   * @throws SQLNotDateException if a field announced being a date isn't.
   * @throws SQLUnsupportedParsingFeatureException if the driver encounter a type it cannot handle.
   * @throws SQLNotNumericException if a field doesn't carry a numeric value when expected to.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  private int compare(DBFRecordBasedResultSet rs)
      throws SQLIllegalParameterException, SQLNoSuchFieldException,
          SQLUnsupportedParsingFeatureException, SQLConnectionClosedException,
          SQLNotNumericException, SQLNotDateException {
    Object value1 = valueOf(rs, getComparand1());
    Object value2 = valueOf(rs, getComparand2());

    // Handle NULL value cases.
    if (value1 == null && value2 == null) return 0;
    else {
      if (value1 == null) return -1;
      else {
        if (value2 == null) return 1;
      }
    }

    assert (value1 != null && value2 != null)
        : "Null values should have been handled in comparison.";

    // If comparands have already the same type, compare them immediately.
    if (value1.getClass().equals(value2.getClass())) {
      return compare(rs, value1, value2);
    } else {
      // Else, attempt to promote their types to something equivalent on the two sides.

      // Promote Short to Integer, Long, Float or Double.
      Integer compare =
          compareIfPromoted(rs, value1, value2, Short.class, Integer.class, Integer::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Short.class, Long.class, Long::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Short.class, Float.class, Float::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Short.class, Double.class, Double::valueOf);

      // Promote Integer to Long, Float or Double.
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Integer.class, Long.class, Long::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Integer.class, Float.class, Float::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Integer.class, Double.class, Double::valueOf);

      // Promote Long to Float or Double.
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Long.class, Float.class, Float::valueOf);
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Long.class, Double.class, Double::valueOf);

      // Promote Float to Double.
      compare =
          compare != null
              ? compare
              : compareIfPromoted(rs, value1, value2, Float.class, Double.class, Double::valueOf);

      // if we are here with still a null value in comparison result, we have found no matching at
      // all.
      // Default to String comparison.
      if (compare == null) {
        String default1 = value1.toString();
        String default2 = value2.toString();
        return compare(rs, default1, default2);
      } else return compare;
    }
  }

  /**
   * Perform a comparison after having attempted to promote compared types : one of the parameter
   * must belongs to one class and the other one to the second one to allow the conversion and
   * comparison to be done, else a null value will be returned, meaning that no promotion were
   * possible.
   *
   * @param <W> the worst class expected, the one that will be promoted to the best class through
   *     the mean of the promoter function.
   * @param <B> the best class expected : one of the parameter will be promoted to this class to
   *     allow comparison to be done.
   * @param rs ResultSet where field values has been taken.
   * @param value1 First value.
   * @param value2 Second value.
   * @param worstHas The worst class expected.
   * @param bestHas The best class expected.
   * @param promoter Promoting function.
   * @return Comparison result, or null if no comparison can be done because a parameter value
   *     cannot be promoted.
   * @throws SQLUnsupportedParsingFeatureException if a comparison fails eventually on a Java type
   *     not belonging to {link #java.lang.Comparable}.
   */
  private <W, B> Integer compareIfPromoted(
      DBFRecordBasedResultSet rs,
      Object value1,
      Object value2,
      Class<W> worstHas,
      Class<B> bestHas,
      Function<W, B> promoter)
      throws SQLUnsupportedParsingFeatureException {
    boolean w1 = value1.getClass().equals(worstHas);
    boolean b1 = value1.getClass().equals(bestHas);
    boolean w2 = value2.getClass().equals(worstHas);
    boolean b2 = value2.getClass().equals(bestHas);

    // if the values has the same class, they should have been already compared. But let's to it.
    if ((w1 && w2) || (b1 && b2)) return compare(rs, value1, value2);
    else {
      // if one value doesn't match to a type, we can't perform the comparison.
      if ((w1 == false && b1 == false) || (w2 == false && b2 == false)) return null;
      else {
        assert ((w1 != b1 && w2 != b2) && (w1 != w2 && b1 != b2))
            : "Parameters are not of different types.";

        // Suppress the warnings because we have done the checkings before.
        @SuppressWarnings("unchecked")
        B sameType1 = w1 ? promoter.apply((W) value1) : (B) value1;
        @SuppressWarnings("unchecked")
        B sameType2 = w2 ? promoter.apply((W) value2) : (B) value2;
        return compare(rs, sameType1, sameType2);
      }
    }
  }

  /**
   * Compare two values of the same type.
   *
   * @param <T> Class of their type.
   * @param rs ResultSet where field values has been taken.
   * @param value1 First comparand.
   * @param value2 Second comparand.
   * @return Result of the comparison.
   * @throws SQLUnsupportedParsingFeatureException if this type doesn't implements {link
   *     #java.lang.Comparable} and cannot be handled by this driver.
   */
  @SuppressWarnings({"rawtypes", "unchecked"}) // Wished : Types are checked by the caller.
  private <T> int compare(DBFRecordBasedResultSet rs, T value1, T value2)
      throws SQLUnsupportedParsingFeatureException {
    Comparable comparable1 = null;
    Comparable comparable2 = null;

    if (value1 instanceof Comparable<?>) {
      comparable1 = (Comparable) value1;
    }

    if (value2 instanceof Comparable<?>) {
      comparable2 = (Comparable) value2;
    }

    // If one of the comparands doesn't belong to java.lang.Comparable, our driver is taken short.
    if (comparable1 == null) {
      String message =
          format(
              Level.WARNING,
              "excp.uncomparable_type",
              value1,
              value1.getClass().getName(),
              rs.getSQL());
      throw new SQLUnsupportedParsingFeatureException(message, rs.getSQL(), rs.getFile());
    }

    if (comparable2 == null) {
      String message =
          format(
              Level.WARNING,
              "excp.uncomparable_type",
              value2,
              value2.getClass().getName(),
              rs.getSQL());
      throw new SQLUnsupportedParsingFeatureException(message, rs.getSQL(), rs.getFile());
    }

    return comparable1.compareTo(comparable2);
  }

  /**
   * Returns the value of a comparand.
   *
   * @param rs ResultSet.
   * @param comparand Comparand.
   * @return Value of that comparand : <br>
   *     - itself, it is a primitive type or an enclosed string. <br>
   *     -a field value if the given string is not enclosed by ' characters : the parser see it a
   *     field name, then.
   * @throws SQLIllegalParameterException if a literal string value is not well enclosed by '...'.
   * @throws SQLNoSuchFieldException if the comparand designs a field name that doesn't exist.
   * @throws SQLNotDateException if a field announced being a date isn't.
   * @throws SQLUnsupportedParsingFeatureException if the driver encounter a type it cannot handle.
   * @throws SQLNotNumericException if a field doesn't carry a numeric value when expected to.
   * @throws SQLConnectionClosedException if the connection is closed.
   */
  private Object valueOf(DBFRecordBasedResultSet rs, Object comparand)
      throws SQLIllegalParameterException, SQLNoSuchFieldException, SQLConnectionClosedException,
          SQLNotNumericException, SQLUnsupportedParsingFeatureException, SQLNotDateException {
    Objects.requireNonNull(
        rs, "ResultSet cannot be null when taking the value of a ResultSet comparand.");
    Objects.requireNonNull(comparand, "Comparand cannot be null.");

    // All comparands that are litterals are returned as they are.
    if (comparand instanceof String == false) return comparand;

    String text = (String) comparand;
    text = text.trim();

    // If the field is enclosed by ' characters, it is considered a litteral too, but these ' are
    // removed before returning the string.
    boolean wannaBeLiteral =
        text.startsWith("'") || text.endsWith("'"); // A ' at the beginning or the end.
    boolean uncompleteLiteral =
        text.startsWith("'") == false
            || text.endsWith("'") == false
            || text.length() < 2; // But not at the two sides, or a string made of a single one.

    if (wannaBeLiteral) {
      if (wannaBeLiteral && uncompleteLiteral) {
        String message = format(Level.WARNING, "excp.illegal_parameter_where", text, rs.getSQL());
        throw new SQLIllegalParameterException(message, rs.getSQL(), rs.getFile(), "literal", text);
      }

      assert (text.indexOf("'") == 0
              && text.indexOf("'") < text.lastIndexOf("'")
              && text.lastIndexOf("'") == text.length() - 1
              && text.length() >= 2)
          : "The litteral string is not enclosed into '...'";

      String literal = text.substring(1, text.length() - 1);
      return literal;
    } else {
      // The string designs a field name, return its value.
      try (DBFBuiltInMemoryResultSetForColumnsListing field =
          (DBFBuiltInMemoryResultSetForColumnsListing) rs.getFieldDesc(text, rs.getSQL())) {
        return valueOf(rs, field);
      }
    }
  }

  /**
   * Returns the field value in a ResultSet.
   *
   * @param rs ResultSet.
   * @param field Field.
   * @return Field value.
   * @throws SQLNotNumericException if the value of a numeric field queried isn't numeric.
   * @throws SQLNoSuchFieldException if a field name doesn't exist in the query.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLUnsupportedParsingFeatureException if our implementation of the driver still not
   *     handle this data type.
   * @throws SQLNotDateException if the value of a date field is not a date.
   */
  private Object valueOf(
      DBFRecordBasedResultSet rs, DBFBuiltInMemoryResultSetForColumnsListing field)
      throws SQLConnectionClosedException, SQLNoSuchFieldException, SQLNotNumericException,
          SQLUnsupportedParsingFeatureException, SQLNotDateException {
    String columnName = field.getString("COLUMN_NAME");

    switch (field.getString("TYPE_NAME")) {
      case "AUTO_INCREMENT":
        return rs.getInt(columnName);

      case "CHAR":
        return rs.getString(columnName);

      case "INTEGER":
        return rs.getInt(columnName);

      case "DATE":
        return rs.getDate(columnName);

      case "DOUBLE":
        return rs.getDouble(columnName);

      case "FLOAT":
        return rs.getFloat(columnName);

      case "DECIMAL":
        {
          // Choose Integer or Long type, if no decimal and that the field is not to big.
          if (field.getInt("DECIMAL_DIGITS") == 0 && field.getInt("COLUMN_SIZE") <= 18) {
            if (field.getInt("COLUMN_SIZE") <= 9) return rs.getInt(columnName);
            else return rs.getLong(columnName);
          }

          return rs.getDouble(columnName);
        }

      case "BOOLEAN":
      case "CURRENCY":
      case "DATETIME":
      case "TIMESTAMP":
      case "MEMO":
      case "PICTURE":
      case "VARIFIELD":
      case "VARIANT":
      case "UNKNOWN":
        String message =
            format(
                Level.WARNING,
                "excp.unparsable_field_type",
                columnName,
                field.getString("TYPE_NAME"),
                rs.getSQL());
        throw new SQLUnsupportedParsingFeatureException(message, rs.getSQL(), rs.getFile());

      default:
        throw new RuntimeException(
            format(Level.WARNING, "assert.unknown_field_type", field.getString("TYPE_NAME")));
    }
  }
}
