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

import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;

/**
 * Special ResultSet listing tables types contained in this DBase 3 (only tables).
 *
 * @author Marc LE BIHAN
 */
public class DBFBuiltInMemoryResultSetForTablesTypesListing extends BuiltInMemoryResultSet {
  /** There's only one result in this ResultSet. */
  private int index = 0;

  /**
   * Construct a ResultSet listing the tables types of a database.
   *
   * @param stmt Statement.
   */
  public DBFBuiltInMemoryResultSetForTablesTypesListing(DBFStatement stmt) {
    super(stmt, "driver list tables types handled by DBase 3");
  }

  /** @see java.sql.ResultSet#getString(java.lang.String) */
  @Override
  public String getString(String columnLabel) {
    logStep("getString", columnLabel);

    switch (columnLabel) {
      case "OBJECTID": // FIXME Documentation of ObjectId for geTabletTypes() has not been found.
        // What are the rules about this field ?
        this.wasNull = false;
        return "1";

      case "TABLE_TYPE": // String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE",
        // "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
        this.wasNull = false;
        return "TABLE"; // and DBase 3 only knows tables.

      default:
        this.wasNull = true;
        return null;
    }
  }

  /** @see java.sql.ResultSet#next() */
  @Override
  public boolean next() throws SQLNoResultException {
    logStep("next");

    if (this.index > 1) {
      throw new SQLNoResultException(
          format(Level.WARNING, "excp.only_one_table_type_handled"),
          "Driver manager asks for table types listing",
          getFile());
    }

    this.index++;
    return (this.index == 1) ? true : false;
  }
}
