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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;


/**
 * This Base ResultSet is only used for Descriptions function (getTables(..) and others functions in in Metadata).
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
public abstract class BuiltInMemoryResultSet extends DBFResultSet {
    /**
     * Construct a ResultSet for descriptions.
     * @param stmt Statement.
     * @param sqlQuery SQLQuery.
     */
    public BuiltInMemoryResultSet(DBFStatement stmt, String sqlQuery) {
        super(stmt, sqlQuery);
    }

    /**
     * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
     * @deprecated Deprecated API (from ResultSet Interface)
     */
    @Deprecated @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw unsupportedOperation("BigDecimal", columnLabel, scale);
    }

    /**
     * @see java.sql.ResultSet#getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return getBigDecimal(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
     */
    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw unsupportedOperation("getBigDecimal", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getDate(int)
     */
    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return getDate(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getDate(java.lang.String)
     */
    @Override
    public Date getDate(String columnLabel) throws SQLException {
        throw unsupportedOperation("getDate", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getDouble(java.lang.String)
     */
    @Override
    public double getDouble(String columnLabel) throws SQLException {
        throw unsupportedOperation("getDouble", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getDouble(int)
     */
    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return getDouble(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getFloat(int)
     */
    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return getFloat(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getFloat(java.lang.String)
     */
    @Override
    public float getFloat(String columnLabel) throws SQLException {
        throw unsupportedOperation("getFloat", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getInt(java.lang.String)
     */
    @Override
    public int getInt(String columnLabel) throws SQLException {
        throw unsupportedOperation("getInt", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getInt(int)
     */
    @Override
    public int getInt(int columnIndex) throws SQLException {
        return getInt(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getLong(java.lang.String)
     */
    @Override public long getLong(String columnLabel) throws SQLException {
        throw unsupportedOperation("getLong", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getLong(int)
     */
    @Override public long getLong(int columnIndex) throws SQLException {
        return getLong(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getShort(java.lang.String)
     */
    @Override public short getShort(String columnLabel) throws SQLException {
        throw unsupportedOperation("getShort", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getShort(int)
     */
    @Override
    public short getShort(int columnIndex) throws SQLException {
        return getShort(getFieldName(columnIndex, this.sql));
    }

    /**
     * @see java.sql.ResultSet#getString(java.lang.String)
     */
    @Override public String getString(String columnLabel) throws SQLException {
        throw unsupportedOperation("getString", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getString(int)
     */
    @Override
    public String getString(int columnIndex) throws SQLException {
        return(getString(getFieldName(columnIndex, this.sql)));
    }

    /**
     * @see java.sql.ResultSet#getMetaData()
     */
    @Override public ResultSetMetaData getMetaData() throws SQLException {
        throw unsupportedOperation("getMetaData");
    }

    /**
     * Returns the table name.
     * @return Table Name.
     */
    protected String getTableName() {
        // The table default to the file name (without its extension .dbf).
        String fileName = getFile().getName();
        int indexDBF = fileName.lastIndexOf(".");
        String tableName = fileName.substring(0, indexDBF);

        return tableName;
    }
}
