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
 * Exception thrown when there is no more result.
 * @author Marc LE BIHAN
 */
public class SQLNoResultException extends SQLException {
    /** Serial ID. */
    private static final long serialVersionUID = -6685966109486353932L;

    /** The SQL Statement that whas attempted. */
    private String sql;

    /** The database that was queried. */
    private File database;

    /**
     * Build the exception.
     * @param message Exception message.
     * @param sqlStatement SQL Statement who encountered the trouble.
     * @param dbf The database that was queried.
     */
    public SQLNoResultException(String message, String sqlStatement, File dbf) {
        super(message);
        this.sql = sqlStatement;
        this.database = dbf;
    }

    /**
     * Returns the SQL statement who encountered the "end of data" alert.
     * @return SQL statement.
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * Returns the database file that was queried.
     * @return The database that was queried.
     */
    public File getDatabase() {
        return this.database;
    }
}
