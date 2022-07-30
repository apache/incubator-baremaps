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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

import org.apache.sis.internal.shapefile.jdbc.AbstractJDBC;


/**
 * Holds all the unimplemented feature of a {@code ResultSet}.
 * This is in order to avoid having a ResultSet implementation of thousand lines and unreadable.
 *
 * <table class="sis">
 *   <caption>Connection default values</caption>
 *   <tr><th>Property</th>                           <th>Value</th></tr>
 *   <tr><td>{@link #getType()}</td>                 <td>{@link Statement#getResultSetType()}</td></tr>
 *   <tr><td>{@link #getConcurrency()}</td>          <td>{@link Statement#getResultSetConcurrency()}</td></tr>
 *   <tr><td>{@link #getHoldability()}</td>          <td>{@link Statement#getResultSetHoldability()}</td></tr>
 *   <tr><td>{@link #getFetchDirection()}</td>       <td>{@link Statement#getFetchDirection()}</td></tr>
 *   <tr><td>{@link #getFetchSize()}</td>            <td>{@link Statement#getFetchSize()}</td></tr>
 *   <tr><td>{@link #isBeforeFirst()}</td>           <td>Compute from {@link #getRow()}</td></tr>
 *   <tr><td>{@link #isFirst()}</td>                 <td>Compute from {@link #getRow()}</td></tr>
 *   <tr><td>{@link #relative(int)}</td>             <td>Use {@link #absolute(int)}</td></tr>
 *   <tr><td>{@link #beforeFirst()}</td>             <td>Use {@link #absolute(int)}</td></tr>
 *   <tr><td>{@link #first()}</td>                   <td>Use {@link #absolute(int)}</td></tr>
 *   <tr><td>{@link #last()}</td>                    <td>Use {@link #absolute(int)}</td></tr>
 *   <tr><td>{@link #afterLast()}</td>               <td>Use {@link #absolute(int)}</td></tr>
 *   <tr><td>{@link #previous()}</td>                <td>Use {@link #relative(int)}</td></tr>
 *   <tr><td>{@link #getNString(int)}</td>           <td>{@link #getString(int)}</td></tr>
 *   <tr><td>{@link #getNCharacterStream(int)}</td>  <td>{@link #getCharacterStream(int)}</td></tr>
 *   <tr><td>{@link #getWarnings()}</td>             <td>{@code null}</td></tr>
 *   <tr><td>{@link #clearWarnings()}</td>           <td>Ignored</td></tr>
 * </table>
 *
 * Furthermore, most methods expecting a column label of type {@code String} first invoke {@link #findColumn(String)},
 * then invoke the method of the same name expecting a column index as an {@code int}.
 *
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
public abstract class AbstractResultSet extends AbstractJDBC implements ResultSet {
    /*
     * Note to developers : this class only offers methods that return unsupported exceptions : methods that are not implemented anywhere.
     * if any implementation is done, even a redirection to another class or method, please move the implementation on the next subclass.
     */

    /**
     * Constructs a new {@code ResultSet} instance.
     */
    public AbstractResultSet() {
    }

    /**
     * @see java.sql.ResultSet#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw unsupportedOperation("getBoolean", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getByte(java.lang.String)
     */
    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw unsupportedOperation("getByte", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getBytes(java.lang.String)
     */
    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw unsupportedOperation("getBytes", columnLabel);
    }

    /**
     * @see java.sql.ResultSet#getTime(java.lang.String)
     */
    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw unsupportedOperation("getTime", columnLabel);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw unsupportedOperation("setFetchDirection", direction);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw unsupportedOperation("setFetchSize", rows);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public String getCursorName() throws SQLException {
        throw unsupportedOperation("getCursorName");
    }

    /**
     * Retrieves the current row number (first row is 1). This method is unsupported by default.
     * Implementing this method will allow {@link #relative(int)} and other methods to work with
     * their default implementation.
     */
    @Override
    public int getRow() throws SQLException {
        throw unsupportedOperation("getRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean isLast() throws SQLException {
        throw unsupportedOperation("isLast");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean isAfterLast() throws SQLException {
        throw unsupportedOperation("isAfterLast");
    }

    /**
     * Moves the cursor to the given row number (first row is 1).
     * Special cases:
     * <ul>
     *   <li>Negative numbers move to an absolute row position with respect to the end of the result set.</li>
     *   <li>-1 moves on the last row.</li>
     *   <li> 0 moves the cursor before the first row.</li>
     * </ul>
     *
     * This method is unsupported by default. Implementing this method will allow
     * {@link #relative(int)} and other methods to work with their default implementation.
     *
     * @return {@code true} if the cursor is on a row.
     */
    @Override
    public boolean absolute(int row) throws SQLException {
        throw unsupportedOperation("absolute", row);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw unsupportedOperation("getRowId", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw unsupportedOperation("getBoolean", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw unsupportedOperation("getByte", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw unsupportedOperation("getBytes", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw unsupportedOperation("getDate", columnIndex, cal);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw unsupportedOperation("getTime", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw unsupportedOperation("getTime", columnIndex, cal);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw unsupportedOperation("getTimestamp", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw unsupportedOperation("getTimestamp", columnIndex, cal);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw unsupportedOperation("getURL", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw unsupportedOperation("getArray", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw unsupportedOperation("getSQLXML", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Object getObject(int columnIndex) throws SQLException {
        throw unsupportedOperation("getObject", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw unsupportedOperation("getObject", columnIndex, map);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw unsupportedOperation("getObject", columnIndex, type);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw unsupportedOperation("getRef", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw unsupportedOperation("getBlob", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw unsupportedOperation("getClob", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw unsupportedOperation("getNClob", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw unsupportedOperation("getNClob", columnLabel);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw unsupportedOperation("getAsciiStream", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw unsupportedOperation("getCharacterStream", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw unsupportedOperation("getUnicodeStream", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw unsupportedOperation("getBinaryStream", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw unsupportedOperation("updateNull", columnIndex);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw unsupportedOperation("updateRowId", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw unsupportedOperation("updateString", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw unsupportedOperation("updateBoolean", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw unsupportedOperation("updateByte", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw unsupportedOperation("updateBytes", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw unsupportedOperation("updateShort", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw unsupportedOperation("updateInt", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw unsupportedOperation("updateLong", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw unsupportedOperation("updateFloat", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw unsupportedOperation("updateDouble", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw unsupportedOperation("updateBigDecimal", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw unsupportedOperation("updateDate", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw unsupportedOperation("updateTime", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw unsupportedOperation("updateTimestamp", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw unsupportedOperation("updateArray", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw unsupportedOperation("updateObject", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw unsupportedOperation("updateObject", columnIndex, x, scaleOrLength);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateSQLXML(int columnIndex, SQLXML x) throws SQLException {
        throw unsupportedOperation("updateSQLXML", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw unsupportedOperation("updateRef", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw unsupportedOperation("updateBlob", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw unsupportedOperation("updateBlob", columnIndex, inputStream, length);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw unsupportedOperation("updateClob", columnIndex, x);
    }

    /**
     * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
     */
    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw unsupportedOperation("updateClob", columnIndex, reader, length);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw unsupportedOperation("updateAsciiStream", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw unsupportedOperation("updateAsciiStream", columnIndex, x, length);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw unsupportedOperation("updateCharacterStream", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw unsupportedOperation("updateCharacterStream", columnIndex, x, length);
    }


    /**
     * Unsupported by default.
     */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw unsupportedOperation("updateBinaryStream", columnIndex, x);
    }


    /**
     * Unsupported by default.
     */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw unsupportedOperation("updateBinaryStream", columnIndex, x, length);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateBlob(int columnIndex, InputStream x) throws SQLException {
        throw unsupportedOperation("updateBlob", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateClob(int columnIndex, Reader x) throws SQLException {
        throw unsupportedOperation("updateClob", columnIndex, x);
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void insertRow() throws SQLException {
        throw unsupportedOperation("insertRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void updateRow() throws SQLException {
        throw unsupportedOperation("updateRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void deleteRow() throws SQLException {
        throw unsupportedOperation("deleteRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void refreshRow() throws SQLException {
        throw unsupportedOperation("refreshRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void cancelRowUpdates() throws SQLException {
        throw unsupportedOperation("cancelRowUpdates");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void moveToInsertRow() throws SQLException {
        throw unsupportedOperation("moveToInsertRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw unsupportedOperation("moveToCurrentRow");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean rowUpdated() throws SQLException {
        throw unsupportedOperation("rowUpdated");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean rowInserted() throws SQLException {
        throw unsupportedOperation("rowInserted");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean rowDeleted() throws SQLException {
        throw unsupportedOperation("rowDeleted");
    }
}
