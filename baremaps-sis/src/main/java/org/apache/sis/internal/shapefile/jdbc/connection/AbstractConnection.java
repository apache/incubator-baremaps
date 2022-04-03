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

package org.apache.sis.internal.shapefile.jdbc.connection;

import java.sql.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.apache.sis.internal.shapefile.jdbc.AbstractJDBC;

/**
 * This base class holds most of the unimplemented feature of a {@code Connection}. This is used in
 * order to avoid having a Connection implementation of thousand lines and unreadable.
 *
 * <table class="sis">
 *   <caption>Connection default values</caption>
 *   <tr><th>Property</th>                           <th>Value</th></tr>
 *   <tr><td>{@link #isReadOnly()}</td>              <td>{@code false}</td></tr>
 *   <tr><td>{@link #getAutoCommit()}</td>           <td>{@code true}</td></tr>
 *   <tr><td>{@link #getNetworkTimeout()}</td>       <td>0</td></tr>
 *   <tr><td>{@link #getTransactionIsolation()}</td> <td>{@link #TRANSACTION_NONE}</td></tr>
 *   <tr><td>{@link #getTypeMap()}</td>              <td>Empty map</td></tr>
 *   <tr><td>{@link #nativeSQL(String)}</td>         <td>No change</td></tr>
 *   <tr><td>{@link #getWarnings()}</td>             <td>{@code null}</td></tr>
 *   <tr><td>{@link #clearWarnings()}</td>           <td>Ignored</td></tr>
 * </table>
 *
 * @author Marc Le Bihan
 * @author Martin Desruisseaux (Geomatys)
 * @version 0.5
 * @since 0.5
 * @module
 */
@SuppressWarnings("unused")
abstract class AbstractConnection extends AbstractJDBC implements Connection {
  /** Constructs a new {@code Connection} instance. */
  AbstractConnection() {}

  /** Unsupported by default. */
  @Override
  public void setCatalog(String catalog) {
    logUnsupportedOperation("setCatalog");
  }

  /** Unsupported by default. */
  @Override
  public String getSchema() throws SQLException {
    throw unsupportedOperation("getSchema");
  }

  /** Unsupported by default. */
  @Override
  public void setSchema(String schema) throws SQLException {
    throw unsupportedOperation("setSchema");
  }

  /** Unsupported by default. */
  @Override
  public Map<String, Class<?>> getTypeMap() {
    return Collections.emptyMap();
  }

  /** Unsupported by default. */
  @Override
  public void setTypeMap(Map<String, Class<?>> map) {
    if (!map.isEmpty()) {
      throw new UnsupportedOperationException("setTypeMap");
    }
  }

  /** Returns {@code true} by default, assuming a driver without write capabilities. */
  @Override
  public boolean isReadOnly() {
    return true;
  }

  /** Unsupported by default. */
  @Override
  public void setReadOnly(boolean readOnly) {
    if (!readOnly) {
      throw new UnsupportedOperationException("setReadOnly");
    }
  }

  /** Defaults to {@link #TRANSACTION_NONE}. */
  @Override
  public int getTransactionIsolation() {
    return TRANSACTION_NONE; // No guarantees of anything.
  }

  /** Unsupported by default. */
  @Override
  public void setTransactionIsolation(int level) {
    if (level != TRANSACTION_NONE) {
      throw new UnsupportedOperationException("setTransactionIsolation");
    }
  }

  /** Defaults to {@code true}, assuming that auto-commit state is not handled. */
  @Override
  public boolean getAutoCommit() {
    return true;
  }

  /**
   * Defaults to ignoring the commit / rollback. The auto-commit mode is assumed fixed to {@code
   * true}.
   */
  @Override
  public void setAutoCommit(boolean autoCommit) {
    log(Level.FINE, "log.auto_commit_ignored", autoCommit);
  }

  /** Unsupported by default. */
  @Override
  public void commit() {
    log(Level.FINE, "log.commit_rollback_ignored");
  }

  /** Unsupported by default. */
  @Override
  public void rollback() {
    log(Level.FINE, "log.commit_rollback_ignored");
  }

  /** Unsupported by default. */
  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    throw unsupportedOperation("rollback");
  }

  /** Unsupported by default. */
  @Override
  public Savepoint setSavepoint() throws SQLException {
    throw unsupportedOperation("setSavepoint");
  }

  /** Unsupported by default. */
  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    throw unsupportedOperation("setSavepoint");
  }

  /** Unsupported by default. */
  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    throw unsupportedOperation("releaseSavepoint");
  }

  /** Unsupported by default. */
  @Override
  public int getHoldability() throws SQLException {
    throw unsupportedOperation("getHoldability");
  }

  /** Unsupported by default. */
  @Override
  public void setHoldability(int holdability) {
    logUnsupportedOperation("setHoldability");
  }

  /** Returns the given string unchanged by default. */
  @Override
  public String nativeSQL(String sql) {
    return sql; // We do nothing at the moment.
  }

  /** Unsupported by default. */
  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw unsupportedOperation("createStatement");
  }

  /** Unsupported by default. */
  @Override
  public Statement createStatement(
      int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    throw unsupportedOperation("createStatement");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public CallableStatement prepareCall(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    throw unsupportedOperation("prepareCall");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    throw unsupportedOperation("prepareStatement");
  }

  /** Unsupported by default. */
  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    throw unsupportedOperation("prepareCall");
  }

  /** Unsupported by default. */
  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw unsupportedOperation("prepareCall");
  }

  /** Unsupported by default. */
  @Override
  public Clob createClob() throws SQLException {
    throw unsupportedOperation("createClob");
  }

  /** Unsupported by default. */
  @Override
  public Blob createBlob() throws SQLException {
    throw unsupportedOperation("createBlob");
  }

  /** Unsupported by default. */
  @Override
  public NClob createNClob() throws SQLException {
    throw unsupportedOperation("createNClob");
  }

  /** Unsupported by default. */
  @Override
  public SQLXML createSQLXML() throws SQLException {
    throw unsupportedOperation("createSQLXML");
  }

  /** Unsupported by default. */
  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    throw unsupportedOperation("createArrayOf");
  }

  /** Unsupported by default. */
  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    throw unsupportedOperation("createStruct");
  }

  /** Unsupported by default. */
  @Override
  public String getClientInfo(String name) throws SQLException {
    throw unsupportedOperation("getClientInfo");
  }

  /** Unsupported by default. */
  @Override
  public Properties getClientInfo() throws SQLException {
    throw unsupportedOperation("getClientInfo");
  }

  /** Unsupported by default. */
  @Override
  public void setClientInfo(String name, String value) {
    logUnsupportedOperation("setClientInfo");
  }

  /** Unsupported by default. */
  @Override
  public void setClientInfo(Properties properties) {
    logUnsupportedOperation("setClientInfo");
  }

  /** Defaults to 0, which means there is no limit. */
  @Override
  public int getNetworkTimeout() {
    return 0; // Means there is no limt.
  }

  /** Unsupported by default. */
  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) {
    logUnsupportedOperation("setNetworkTimeout");
  }

  /** Unsupported by default. */
  @Override
  public void abort(Executor executor) throws SQLException {
    throw unsupportedOperation("abort");
  }
}
