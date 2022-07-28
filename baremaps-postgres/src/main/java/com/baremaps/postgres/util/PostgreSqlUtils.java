// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.util.Optional;

public final class PostgreSqlUtils {

    private PostgreSqlUtils() {
    }

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
    public static String getFullyQualifiedTableName(@Nullable String schemaName, String tableName, boolean usePostgresQuoting) {
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