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
package org.apache.sis.internal.shapefile.jdbc;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;


/**
 * Base class for each JDBC class.
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
public abstract class AbstractJDBC implements Wrapper {
    /**
     * Constructs a new instance of a JDBC interface.
     */
    public AbstractJDBC() {
    }

    /**
     * Returns the JDBC interface implemented by this class.
     * This is used for formatting error messages.
     * @return The JDBC interface implemented by this class.
     */
    abstract protected Class<?> getInterface();

    /**
     * Unsupported by default.
     * @param iface the type of the wrapped object.
     * @return The wrapped object.
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw unsupportedOperation("unwrap", iface);
    }

    /**
     * Defaults to {@code null}.
     * @return SQL Warning.
     */
    public SQLWarning getWarnings() {
        return null;
    }

    /**
     * Defaults to nothing, since there are no SQL warnings.
     */
    public void clearWarnings() {
    }

    /**
     * Returns an unsupported operation exception to be thrown.
     * @param methodOrWishedFeatureName The feature / call the caller attempted.
     * @return the not supported feature exception.
     * @throws SQLFeatureNotSupportedException the not supported feature.
     */
    public final SQLFeatureNotSupportedException unsupportedOperation(final String methodOrWishedFeatureName) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Unsupported driver feature");
    }

    /**
     * Returns an unsupported operation exception to be thrown : this exception add parameters sent to the method that isn't implemented.
     * @param methodOrWishedFeatureName The feature / call the caller attempted.
     * @param args Arguments that where sent to the unimplemented function.
     * @return the not supported feature exception.
     * @throws SQLFeatureNotSupportedException the not supported feature.
     */
    public final SQLFeatureNotSupportedException unsupportedOperation(final String methodOrWishedFeatureName, Object... args) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Unsupported driver feature");
    }

    /**
     * Returns the Database File.
     * @return Database File.
     */
    abstract protected File getFile();

    /**
     * Concat arguments in a StringBuffer.
     * @param args arguments.
     * @return Arguments.
     */
    private StringBuffer arguments(Object... args) {
        StringBuffer arguments = new StringBuffer();

        for(Object arg : args) {
            arguments.append(arguments.length() == 0 ? "" : ", ");   // Separator if needed.
            arguments.append(arg instanceof String ? "\"" : "");     // Enclosing " for String, if needed.
            arguments.append(arg == null ? "null" : arg.toString()); // String value of the argument.
            arguments.append(arg instanceof String ? "\"" : "");     // Enclosing " for String, if needed.
        }

        return arguments;
    }
}
