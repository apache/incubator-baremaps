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


/**
 * Provides a simple DataType class.
 *
 * @author  Travis L. Pinney
 * @version 0.5
 *
 * @see <a href="http://www.clicketyclick.dk/databases/xbase/format/data_types.html">Xbase Data Types</a>
 *
 * @since 0.5
 * @module
 */
public enum DBaseDataType {
    /** Character (less than 254 characters). */
    Character('C'),

    /** Number (less than 18 characters, can include sign and decimal). */
    Number('N'),

    /** Logical (3 way, ? Y,y,T,t  N,n,F,f). */
    Logical('L'),

    /** Date (YYYYMMDD format). */
    Date('D'),

    /** Memo (Pointer to ASCII text field). */
    Memo('M'),

    /** Floating point (20 digits). */
    FloatingPoint('F'),

    // CharacterNameVariable("?"),  //1-254 Characters

    /** Picture (memo). */
    Picture('P'),

    /** Currency (Foxpro). */
    Currency('Y'),

    /** Date time (32 bit little-endian Julian date, 32 byte little endian milliseconds since midnight). */
    DateTime('T'),

    /** Integer (4 byte little endian). */
    Integer('I'),

    /** Varifield (???). */
    VariField('V'),

    /** Variant (???). */
    Variant('X'),

    /** Time stamp (see url). */
    TimeStamp('@'),

    /** Double. */
    Double('O'),

    /** Auto increment. */
    AutoIncrement('+');

    /** Data type. */
    public final char datatype;

    /**
     * Construct a datatype.
     * @param type Data type.
     */
    DBaseDataType(char type) {
        this.datatype = type;
    }

    /**
     * Return the Datatype enum of a code.
     * @param code Character code describing the dbf datatype.
     * @return Datatype.
     */
    public static DBaseDataType valueOfDataType(char code) {
        for (DBaseDataType v : values()) {
            if (v.datatype == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Enum datatype is incorrect");
    }
}
