/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.shapefile.jdbc.resultset;

import java.io.File;
import java.sql.SQLException;

/**
 * Exception thrown when a column name is not found in a request or in the database structure.
 * @author Marc LE BIHAN
 */
public class SQLNoSuchFieldException extends SQLException {
    /** Serial ID. */
    private static final long serialVersionUID = 1118679516090878177L;

    /** The SQL Statement (if known). */
    private String sql;

    /** The database file. */
    private File database;

    /** Column name that doesn't exist. */
    private String columnName;

    /**
     * Build the exception.
     * @param message Exception message.
     * @param sqlStatement SQL Statement who encountered the trouble, if known.
     * @param dbf The database that was queried.
     * @param colName The column name that doesn't exist.
     */
    public SQLNoSuchFieldException(String message, String sqlStatement, File dbf, String colName) {
        super(message);
        this.sql = sqlStatement;
        this.database = dbf;
        this.columnName = colName;
    }

    /**
     * Returns the SQL statement.
     * @return SQL statement or null.
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * Returns the column name that doesn't exist.
     * @return Column name.
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Returns the database file.
     * @return Database file.
     */
    public File getDatabase() {
        return this.database;
    }
}
