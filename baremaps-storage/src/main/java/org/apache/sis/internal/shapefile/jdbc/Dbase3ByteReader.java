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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.sis.internal.shapefile.jdbc.resultset.SQLIllegalColumnIndexException;
import org.apache.sis.internal.shapefile.jdbc.resultset.SQLNoSuchFieldException;
import org.apache.sis.feature.AbstractFeature;

/**
 * Database byte reader contract. Used to allow refactoring of core byte management of a DBase file.
 * @author Marc LE BIHAN
 */
public interface Dbase3ByteReader {
    /**
     * Close the MappedByteReader.
     * @throws IOException if the close operation fails.
     */
    public void close() throws IOException;

    /**
     * Checks if the ByteReader is closed.
     * @return true if it is closed.
     */
    public boolean isClosed();

    /**
     * Returns the fields descriptors in their binary format.
     * @return Fields descriptors.
     */
    public List<DBase3FieldDescriptor> getFieldsDescriptors();

    /**
     * Returns the column index for the given column name.
     * The default implementation of all methods expecting a column label will invoke this method.
     * @param columnLabel The name of the column.
     * @param sql For information, the SQL statement that is attempted.
     * @return The index of the given column name : first column is 1.
     * @throws SQLNoSuchFieldException if there is no field with this name in the query.
     */
    public int findColumn(String columnLabel, String sql) throws SQLNoSuchFieldException;

    /**
     * Returns the charset.
     * @return Charset.
     */
    public Charset getCharset();

    /**
     * Returns the column count of the unique table of the DBase 3.
     * @return Column count.
     */
    public int getColumnCount();

    /**
     * Return a field name.
     * @param columnIndex Column index.
     * @param sql For information, the SQL statement that is attempted.
     * @return Field Name.
     * @throws SQLIllegalColumnIndexException if the index is out of bounds.
     */
    public String getFieldName(int columnIndex, String sql) throws SQLIllegalColumnIndexException;

    /**
     * Returns the database last update date.
     * @return Date of the last update.
     */
    public Date getDateOfLastUpdate();
    
    /**
     * Returns the first record position, in bytes, in the DBase file.
     * @return First record position.
     */
    public short getFirstRecordPosition();

    /**
     * Returns the length (in bytes) of one record in this DBase file, including the delete flag. 
     * @return Record length.
     */
    public short getRecordLength();
    
    /**
     * Returns the record count.
     * @return Record count.
     */
    public int getRowCount();

    /**
     * Returns the current record number.
     * @return Current record number.
     */
    public int getRowNum();

    /**
     * Load a row into a feature.
     * @param feature Feature to fill.
     */
    public void loadRowIntoFeature(AbstractFeature feature);

    /**
     * Checks if a next row is available. Warning : it may be a deleted one.
     * @return true if a next row is available.
     */
    public boolean nextRowAvailable();

    /**
     * Read the next row as a set of objects.
     * @return Map of field name / object value.
     */
    public Map<String, byte[]> readNextRowAsObjects();
}
