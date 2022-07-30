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
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;

import org.apache.sis.internal.shapefile.jdbc.resultset.DBFBuiltInMemoryResultSetForColumnsListing;
import org.apache.sis.internal.shapefile.jdbc.resultset.SQLIllegalColumnIndexException;
import org.apache.sis.internal.shapefile.jdbc.resultset.SQLNoSuchFieldException;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.internal.shapefile.jdbc.resultset.SQLNotDateException;
import org.apache.sis.internal.shapefile.jdbc.resultset.SQLNotNumericException;


/**
 * Reader of a Database Binary content by the way of a {@link java.nio.MappedByteBuffer}
 *
 * @author Marc Le Bihan
 * @version 0.5
 * @module
 * @since 0.5
 */
public class MappedByteReader extends AbstractDbase3ByteReader implements AutoCloseable {

  /**
   * List of field descriptors.
   */
  private List<DBase3FieldDescriptor> fieldsDescriptors = new ArrayList<>();

  /**
   * Connection properties.
   */
  private Properties info;

  /**
   * Construct a mapped byte reader on a file.
   *
   * @param dbase3File      File.
   * @param connectionInfos Connection properties, maybe null.
   * @throws SQLInvalidDbaseFileFormatException if the database seems to be invalid.
   * @throws SQLDbaseFileNotFoundException      if the Dbase file has not been found.
   */
  public MappedByteReader(File dbase3File, Properties connectionInfos)
      throws SQLInvalidDbaseFileFormatException, SQLDbaseFileNotFoundException {
    super(dbase3File);
    this.info = connectionInfos;

    // React to special features asked.
    if (this.info != null) {
      // Sometimes, DBF files have a wrong charset, or more often : none, and you have to specify it.
      String recordCharset = (String) this.info.get("record_charset");

      if (recordCharset != null) {
        Charset cs = Charset.forName(recordCharset);
        setCharset(cs);
      }
    }

    loadDescriptor();
  }

  /**
   * Load a row into a feature.
   *
   * @param feature Feature to fill.
   */
  @Override
  public void loadRowIntoFeature(AbstractFeature feature) {
    // TODO: ignore deleted records
    getByteBuffer().get(); // denotes whether deleted or current
    // read first part of record

    for (DBase3FieldDescriptor fd : this.fieldsDescriptors) {
      byte[] data = new byte[fd.getLength()];
      getByteBuffer().get(data);

      int length = data.length;
      while (length != 0 && Byte.toUnsignedInt(data[length - 1]) <= ' ') {
        length--;
      }

      String value = new String(data, 0, length);

      // TODO: move somewhere else
      Object object = switch (fd.getType()) {
        case Character -> value;
        case Number -> getNumber(fd, value);
        case Currency -> Double.parseDouble(value.trim());
        case Integer -> Integer.parseInt(value.trim());
        case Double -> Double.parseDouble(value.trim());
        case AutoIncrement -> Integer.parseInt(value.trim());
        case Logical -> value;
        case Date -> value;
        case Memo -> value;
        case FloatingPoint -> value;
        case Picture -> value;
        case VariField -> value;
        case Variant -> value;
        case TimeStamp -> value;
        case DateTime -> value;
      };

      feature.setPropertyValue(fd.getName(), object);
    }
  }

  private Object getNumber(DBase3FieldDescriptor fd, String value) {
    if (fd.getDecimalCount() == 0) {
      return Long.parseLong(value.trim());
    } else {
      return Double.parseDouble(value.trim());
    }
  }

  /**
   * Checks if a next row is available. Warning : it may be a deleted one.
   *
   * @return true if a next row is available.
   */
  @Override
  public boolean nextRowAvailable() {
    // 1) Check for remaining bytes.
    if (getByteBuffer().hasRemaining() == false) {
      return false;
    }

    // 2) Check that the immediate next byte read isn't the EOF signal.
    byte eofCheck = getByteBuffer().get();

    boolean isEOF = (eofCheck == 0x1A);

    if (eofCheck == 0x1A) {
      return false;
    } else {
      // Return one byte back.
      int position = getByteBuffer().position();
      getByteBuffer().position(position - 1);
      return true;
    }
  }

  /**
   * Returns the record number of the last record red.
   *
   * @return The record number.
   */
  @Override
  public int getRowNum() {
    int position = getByteBuffer().position();
    int recordNumber =
        (position - Short.toUnsignedInt(this.firstRecordPosition)) / Short.toUnsignedInt(this.recordLength);
    return recordNumber;
  }

  /**
   * Read the next row as a set of objects.
   *
   * @return Map of field name / object value.
   */
  @Override
  public Map<String, byte[]> readNextRowAsObjects() {
    // TODO: ignore deleted records
    /* byte isDeleted = */
    getByteBuffer().get(); // denotes whether deleted or current

    // read first part of record
    HashMap<String, byte[]> fieldsValues = new HashMap<>();

    for (DBase3FieldDescriptor fd : this.fieldsDescriptors) {
      byte[] data = new byte[fd.getLength()];
      getByteBuffer().get(data);

      // Trim the bytes right.
      int length = data.length;

      while (length != 0 && Byte.toUnsignedInt(data[length - 1]) <= ' ') {
        length--;
      }

      if (length != data.length) {
        byte[] dataTrimmed = new byte[length];

        for (int index = 0; index < length; index++) {
          dataTrimmed[index] = data[index];
        }

        fieldsValues.put(fd.getName(), dataTrimmed);
      } else {
        fieldsValues.put(fd.getName(), data);
      }
    }

    return fieldsValues;
  }

  /**
   * Loading the database file content from binary .dbf file.
   *
   * @throws SQLInvalidDbaseFileFormatException if descriptor is not readable.
   */
  private void loadDescriptor() throws SQLInvalidDbaseFileFormatException {
    try {
      this.dbaseVersion = getByteBuffer().get();
      getByteBuffer().get(this.dbaseLastUpdate);

      getByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
      this.rowCount = getByteBuffer().getInt();
      this.firstRecordPosition = getByteBuffer().getShort();
      this.recordLength = getByteBuffer().getShort();
      getByteBuffer().order(ByteOrder.BIG_ENDIAN);

      getByteBuffer().get(this.reservedFiller1);
      this.reservedIncompleteTransaction = getByteBuffer().get();
      this.reservedEncryptionFlag = getByteBuffer().get();
      getByteBuffer().get(this.reservedFreeRecordThread);
      getByteBuffer().get(this.reservedMultiUser);
      this.reservedMDXFlag = getByteBuffer().get();

      // Translate code page value to a known charset.
      this.codePage = getByteBuffer().get();

      if (this.charset == null) {
        try {
          this.charset = toCharset(this.codePage);
        } catch (UnsupportedCharsetException e) {
          // Warn the caller that he will have to perform is own conversions.
        }
      }

      getByteBuffer().get(this.reservedFiller2);

      while (getByteBuffer().position() < this.firstRecordPosition - 1) {
        DBase3FieldDescriptor fd = new DBase3FieldDescriptor(getByteBuffer());
        this.fieldsDescriptors.add(fd);
        // loop until you hit the 0Dh field terminator
      }

      this.descriptorTerminator = getByteBuffer().get();

      // If the last character read after the field descriptor isn't 0x0D, the expected mark has not been found and the DBF is corrupted.
      if (this.descriptorTerminator != 0x0D) {
        throw new SQLInvalidDbaseFileFormatException("File descriptor problem");
      }
    } catch (BufferUnderflowException e) {
      // This exception doesn't denote a trouble of file opening because the file has been checked before
      // the calling of this private function.
      // Therefore, an internal structure problem cause maybe a premature End of file or anything else, but the only thing
      // we can conclude is : we are not before a device trouble, but a file format trouble.
      throw new SQLInvalidDbaseFileFormatException("File descriptor problem");
    }
  }

  /**
   * Returns the fields descriptors in their binary format.
   *
   * @return Fields descriptors.
   */
  @Override
  public List<DBase3FieldDescriptor> getFieldsDescriptors() {
    return this.fieldsDescriptors;
  }

  /**
   * Return a field name.
   *
   * @param columnIndex Column index.
   * @param sql         For information, the SQL statement that is attempted.
   * @return Field Name.
   * @throws SQLIllegalColumnIndexException if the index is out of bounds.
   */
  @Override
  public String getFieldName(int columnIndex, String sql) throws SQLIllegalColumnIndexException {
    return getField(columnIndex, sql).getName();
  }

  /**
   * @see org.apache.sis.internal.shapefile.jdbc.Dbase3ByteReader#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return this.fieldsDescriptors.size();
  }

  /**
   * Returns the column index for the given column name. The default implementation of all methods expecting a column
   * label will invoke this method.
   *
   * @param columnLabel The name of the column.
   * @param sql         For information, the SQL statement that is attempted.
   * @return The index of the given column name : first column is 1.
   * @throws SQLNoSuchFieldException if there is no field with this name in the query.
   */
  @Override
  public int findColumn(String columnLabel, String sql) throws SQLNoSuchFieldException {
    // If the column name is null, no search is needed.
    if (columnLabel == null) {
      throw new SQLNoSuchFieldException("No such column in resultset", sql, getFile(), columnLabel);
    }

    // Search the field among the fields descriptors.
    for (int index = 0; index < this.fieldsDescriptors.size(); index++) {
      if (this.fieldsDescriptors.get(index).getName().equals(columnLabel)) {
        return index + 1;
      }
    }

    // If we are here, we haven't found our field. Throw an exception.
    throw new SQLNoSuchFieldException("No such column in resultset", sql, getFile(), columnLabel);
  }

  /**
   * Returns the field descriptor of a given ResultSet column index.
   *
   * @param columnIndex Column index, first column is 1, second is 2, etc.
   * @param sql         For information, the SQL statement that is attempted.
   * @return Field Descriptor.
   * @throws SQLIllegalColumnIndexException if the index is out of bounds.
   */
  private DBase3FieldDescriptor getField(int columnIndex, String sql) throws SQLIllegalColumnIndexException {
    if (columnIndex < 1 || columnIndex > getColumnCount()) {
      throw new SQLIllegalColumnIndexException("Illegal column index", sql, getFile(), columnIndex);
    }

    return this.fieldsDescriptors.get(columnIndex - 1);
  }
}
