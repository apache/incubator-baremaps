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
package org.apache.sis.internal.shapefile.jdbc.statement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.sis.internal.shapefile.jdbc.AbstractJDBC;


/**
 * This base class holds most of the unimplemented feature of a {@code Statement}.
 * This is used in order to avoid having a Statement implementation of thousand lines and unreadable.
 *
 * <table class="sis">
 *   <caption>Connection default values</caption>
 *   <tr><th>Property</th>                           <th>Value</th></tr>
 *   <tr><td>{@link #getResultSetType()}</td>        <td>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</td></tr>
 *   <tr><td>{@link #getResultSetConcurrency()}</td> <td>{@link ResultSet#CONCUR_READ_ONLY}</td></tr>
 *   <tr><td>{@link #getResultSetHoldability()}</td> <td>{@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</td></tr>
 *   <tr><td>{@link #getFetchDirection()}</td>       <td>{@link ResultSet#FETCH_FORWARD}</td></tr>
 *   <tr><td>{@link #getQueryTimeout()}</td>         <td>0</td></tr>
 *   <tr><td>{@link #isPoolable()}</td>              <td>{@code false}</td></tr>
 *   <tr><td>{@link #setPoolable(boolean)}</td>      <td>Ignored</td></tr>
 *   <tr><td>{@link #getWarnings()}</td>             <td>{@code null}</td></tr>
 *   <tr><td>{@link #clearWarnings()}</td>           <td>Ignored</td></tr>
 * </table>
 *
 * @author  Marc Le Bihan
 * @author  Martin Desruisseaux (Geomatys)
 * @version 0.5
 * @since   0.5
 * @module
 */
@SuppressWarnings("unused")
abstract class AbstractStatement extends AbstractJDBC implements Statement {
    /**
     * Constructs a new {@code Statement} instance.
     */
    AbstractStatement() {
    }

    /**
     * Default to {@link ResultSet#TYPE_SCROLL_SENSITIVE}, meaning that a change in the underlying
     * database may affect the result set.
     */
    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_SCROLL_SENSITIVE;
    }

    /**
     * Default to {@link ResultSet#CONCUR_READ_ONLY}, which is a conservative vale meaning
     * that the underlying database should not be updated while we iterate in a result set.
     */
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * Defaults to {@link ResultSet#CLOSE_CURSORS_AT_COMMIT}, which seems the most conservative option.
     */
    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int getMaxFieldSize() throws SQLException {
        throw unsupportedOperation("getMaxFieldSize");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw unsupportedOperation("setMaxFieldSize");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int getFetchSize() throws SQLException {
        throw unsupportedOperation("getFetchSize");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw unsupportedOperation("setFetchSize");
    }

    /**
     * Defaults to {@link ResultSet#FETCH_FORWARD}.
     */
    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw unsupportedOperation("setFetchDirection");
        }
    }

    /**
     * Defaults to 0, meaning to limit.
     */
    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw unsupportedOperation("setQueryTimeout");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw unsupportedOperation("setEscapeProcessing");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void setCursorName(String name) throws SQLException {
        throw unsupportedOperation("setCursorName");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw unsupportedOperation("execute with autoGeneratedKeys");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw unsupportedOperation("execute with columnIndexes");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw unsupportedOperation("execute with columnNames");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw unsupportedOperation("executeUpdate");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw unsupportedOperation("executeUpdate");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw unsupportedOperation("executeUpdate");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw unsupportedOperation("executeUpdate");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void addBatch(String sql) throws SQLException {
        throw unsupportedOperation("addBatch");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void clearBatch() throws SQLException {
        throw unsupportedOperation("clearBatch");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public int[] executeBatch() throws SQLException {
        throw unsupportedOperation("executeBatch");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean getMoreResults() throws SQLException {
        throw unsupportedOperation("getMoreResults");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw unsupportedOperation("getMoreResults");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw unsupportedOperation("getGeneratedKeys");
    }

    /**
     * Defaults to {@code false} since simple statement imlementations are not poolable.
     */
    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    /**
     * Ignored by default since this method is only a hint.
     */
    @Override
    public void setPoolable(boolean poolable) throws SQLException {
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void cancel() throws SQLException {
        throw unsupportedOperation("cancel");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public void closeOnCompletion() throws SQLException {
        throw unsupportedOperation("closeOnCompletion");
    }

    /**
     * Unsupported by default.
     */
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw unsupportedOperation("isCloseOnCompletion");
    }
}
