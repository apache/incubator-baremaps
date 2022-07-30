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

import java.sql.SQLNonTransientException;


/**
 * Thrown when the DBF file format seems to be invalid.
 *
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
public class SQLInvalidDbaseFileFormatException extends SQLNonTransientException {
    /** Serial UID. */
    private static final long serialVersionUID = 3924612615300490837L;

    /**
     * Construct an exception.
     * @param message Message of the exception.
     */
    public SQLInvalidDbaseFileFormatException(String message) {
        super(message);
    }

    /**
     * Construct an exception.
     * @param message Message of the exception.
     * @param cause Root cause of the exception.
     */
    public SQLInvalidDbaseFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
