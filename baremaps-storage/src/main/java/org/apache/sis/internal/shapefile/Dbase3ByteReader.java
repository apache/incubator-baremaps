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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.sis.feature.AbstractFeature;

/**
 * Database byte reader contract. Used to allow refactoring of core byte management of a DBase file.
 *
 * @author Marc LE BIHAN
 */
public interface Dbase3ByteReader {

  /**
   * Close the MappedByteReader.
   *
   * @throws IOException if the close operation fails.
   */
  void close() throws IOException;

  /**
   * Checks if the ByteReader is closed.
   *
   * @return true if it is closed.
   */
  boolean isClosed();

  /**
   * Returns the fields descriptors in their binary format.
   *
   * @return Fields descriptors.
   */
  List<DBase3FieldDescriptor> getFieldsDescriptors();

  /**
   * Returns the charset.
   *
   * @return Charset.
   */
  Charset getCharset();

  /**
   * Returns the column count of the unique table of the DBase 3.
   *
   * @return Column count.
   */
  int getColumnCount();

  /**
   * Return a field name.
   *
   * @param columnIndex Column index.
   * @param sql         For information, the SQL statement that is attempted.
   * @return Field Name.
   */
  String getFieldName(int columnIndex, String sql);

  /**
   * Returns the database last update date.
   *
   * @return Date of the last update.
   */
  Date getDateOfLastUpdate();

  /**
   * Returns the first record position, in bytes, in the DBase file.
   *
   * @return First record position.
   */
  short getFirstRecordPosition();

  /**
   * Returns the length (in bytes) of one record in this DBase file, including the delete flag.
   *
   * @return Record length.
   */
  short getRecordLength();

  /**
   * Returns the record count.
   *
   * @return Record count.
   */
  int getRowCount();

  /**
   * Returns the current record number.
   *
   * @return Current record number.
   */
  int getRowNum();

  /**
   * Load a row into a feature.
   *
   * @param feature Feature to fill.
   */
  void loadRowIntoFeature(AbstractFeature feature);

  /**
   * Checks if a next row is available. Warning : it may be a deleted one.
   *
   * @return true if a next row is available.
   */
  boolean nextRowAvailable();

  /**
   * Read the next row as a set of objects.
   *
   * @return Map of field name / object value.
   */
  Map<String, byte[]> readNextRowAsObjects();
}
