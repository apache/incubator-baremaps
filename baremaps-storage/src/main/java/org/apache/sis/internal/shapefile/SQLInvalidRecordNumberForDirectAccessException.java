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
 * Thrown when a record number is invalid to do a direct access in a Shapefile or a DBase III file.
 *
 * @author  Marc Le Bihan
 * @version 0.7
 * @since   0.7
 * @module
 */
public class SQLInvalidRecordNumberForDirectAccessException extends SQLNonTransientException {
    /** Serial UID. */
    private static final long serialVersionUID = 6828362742568015813L;
    
    /** Wrong record number. */
    private int wrongRecordNumber;

    /**
     * Construct an exception.
     * @param number Wrong record number value.
     * @param message Message of the exception.
     */
    public SQLInvalidRecordNumberForDirectAccessException(int number, String message) {
        super(message);
        this.wrongRecordNumber = number;
    }

    /**
     * Construct an exception.
     * @param number Wrong record number value.
     * @param message Message of the exception.
     * @param cause Root cause of the exception.
     */
    public SQLInvalidRecordNumberForDirectAccessException(int number, String message, Throwable cause) {
        super(message, cause);
        this.wrongRecordNumber = number;
    }
    
    /**
     * Returns the wrong record number value.
     * @return Record number.
     */
    public int getWrongRecordNumber() {
        return this.wrongRecordNumber;
    }
}
