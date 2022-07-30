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

import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;


/**
 * Special ResultSet listing schemas contained in this DBase 3 (they are none).
 * @author Marc LE BIHAN
 */
public class DBFBuiltInMemoryResultSetForSchemaListing extends BuiltInMemoryResultSet {
    /**
     * Construct a ResultSet listing the tables of a database.
     * @param stmt Statement.
     */
    public DBFBuiltInMemoryResultSetForSchemaListing(DBFStatement stmt) {
        super(stmt, "driver list schemas in this DBase file");
    }

    /**
     * @see java.sql.ResultSet#next()
     */
    @Override public boolean next() {
        return false;
    }

    /**
     * @see java.sql.ResultSet#wasNull()
     */
    @Override
    public boolean wasNull() {
        return true;
    }
}
