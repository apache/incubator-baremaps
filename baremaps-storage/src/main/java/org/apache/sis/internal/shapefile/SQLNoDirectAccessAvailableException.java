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
package org.apache.sis.internal.shapefile;

import java.sql.SQLNonTransientException;


/**
 * Thrown when a shapefile or a DBase III file cannot allow a direct acces (because it has no index).
 *
 * @author  Marc Le Bihan
 * @version 0.7
 * @since   0.7
 * @module
 */
public class SQLNoDirectAccessAvailableException extends SQLNonTransientException {
    /** Serial UID. */
    private static final long serialVersionUID = 16719558033196219L;

    /**
     * Construct an exception.
     * @param message Message of the exception.
     */
    public SQLNoDirectAccessAvailableException(String message) {
        super(message);
    }

    /**
     * Construct an exception.
     * @param message Message of the exception.
     * @param cause Root cause of the exception.
     */
    public SQLNoDirectAccessAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
