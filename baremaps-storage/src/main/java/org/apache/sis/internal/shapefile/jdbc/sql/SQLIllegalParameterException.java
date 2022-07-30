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
package org.apache.sis.internal.shapefile.jdbc.sql;

import java.io.File;
import java.sql.SQLException;

/**
 * Exception thrown when a parameter is invalid.
 * @author Marc LE BIHAN
 */
public class SQLIllegalParameterException extends SQLException {
    /** Serial ID. */
    private static final long serialVersionUID = -3173798942882143448L;

    /** The SQL Statement (if known). */
    private String sql;

    /** The database file. */
    private File database;

    /** Parameter name (if known) that is invalid. */
    private String parameterName;

    /** Parameter value that is invalid. */
    private String parameterValue;

    /**
     * Build the exception.
     * @param message Exception message.
     * @param sqlStatement SQL Statement who encountered the trouble, if known.
     * @param dbf The database that was queried.
     * @param name The parameter name that is invalid.
     * @param value The parameter value that is invalid.
     */
    public SQLIllegalParameterException(String message, String sqlStatement, File dbf, String name, String value) {
        super(message);
        this.sql = sqlStatement;
        this.database = dbf;
        this.parameterName = name;
        this.parameterValue = value;
    }

    /**
     * Returns the SQL statement.
     * @return SQL statement or null.
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * Returns the parameter name that is invalid, if known.
     * @return Parameter name.
     */
    public String geParameterName() {
        return this.parameterName;
    }

    /**
     * Returns the parameter value that is invalid.
     * @return Parameter name.
     */
    public String geParameterValue() {
        return this.parameterValue;
    }

    /**
     * Returns the database file.
     * @return Database file.
     */
    public File getDatabase() {
        return this.database;
    }
}
