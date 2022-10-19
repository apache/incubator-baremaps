/*
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

package org.apache.baremaps.postgres.util;



import java.sql.Connection;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGConnection;

public final class PostgreSqlUtils {

  private PostgreSqlUtils() {}

  public static Optional<PGConnection> tryGetPGConnection(final Connection connection) {
    final Optional<PGConnection> fromCast = tryCastConnection(connection);
    if (fromCast.isPresent()) {
      return fromCast;
    }
    return tryUnwrapConnection(connection);
  }

  private static Optional<PGConnection> tryCastConnection(final Connection connection) {
    if (connection instanceof PGConnection) {
      return Optional.of((PGConnection) connection);
    }
    return Optional.empty();
  }

  private static Optional<PGConnection> tryUnwrapConnection(final Connection connection) {
    try {
      if (connection.isWrapperFor(PGConnection.class)) {
        return Optional.of(connection.unwrap(PGConnection.class));
      }
    } catch (Exception e) {
      // do nothing
    }
    return Optional.empty();
  }

  public static final char QuoteChar = '"';

  public static String quoteIdentifier(String identifier) {
    return requiresQuoting(identifier) ? (QuoteChar + identifier + QuoteChar) : identifier;
  }

  @SuppressWarnings("NullAway")
  public static String getFullyQualifiedTableName(@Nullable String schemaName, String tableName,
      boolean usePostgresQuoting) {
    if (usePostgresQuoting) {
      return StringUtils.isNullOrWhiteSpace(schemaName) ? quoteIdentifier(tableName)
          : String.format("%s.%s", quoteIdentifier(schemaName), quoteIdentifier(tableName));
    }

    if (StringUtils.isNullOrWhiteSpace(schemaName)) {
      return tableName;
    }

    return String.format("%1$s.%2$s", schemaName, tableName);
  }

  private static boolean requiresQuoting(String identifier) {

    char first = identifier.charAt(0);
    char last = identifier.charAt(identifier.length() - 1);

    if (first == QuoteChar && last == QuoteChar) {
      return false;
    }

    return true;
  }
}
