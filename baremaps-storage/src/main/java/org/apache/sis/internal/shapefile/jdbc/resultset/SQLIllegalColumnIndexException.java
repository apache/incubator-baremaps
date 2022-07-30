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
 * Exception thrown when a column index is invalid.
 * @author Marc LE BIHAN
 */
public class SQLIllegalColumnIndexException extends SQLException {
    /** Serial ID. */
    private static final long serialVersionUID = 7525295716068215255L;

    /** The SQL Statement (if known). */
    private String sql;

    /** The database file. */
    private File database;

    /** Column Index that is invalid. */
    private int columnIndex;

    /**
     * Build the exception.
     * @param message Exception message.
     * @param sqlStatement SQL Statement who encountered the trouble, if known.
     * @param dbf The database that was queried.
     * @param colIndex The column index that is invalid.
     */
    public SQLIllegalColumnIndexException(String message, String sqlStatement, File dbf, int colIndex) {
        super(message);
        this.sql = sqlStatement;
        this.database = dbf;
        this.columnIndex = colIndex;
    }

    /**
     * Returns the SQL statement.
     * @return SQL statement or null.
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * Returns the column index.
     * @return Column index.
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Returns the database file.
     * @return Database file.
     */
    public File getDatabase() {
        return this.database;
    }
}
