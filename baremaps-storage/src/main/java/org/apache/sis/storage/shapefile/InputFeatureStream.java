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
package org.apache.sis.storage.shapefile;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLFeatureNotSupportedException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.*;
import org.apache.sis.storage.DataStoreClosedException;
import org.apache.sis.feature.AbstractFeature;

/**
 * Input Stream of features.
 *
 * <div class="warning">This is an experimental class,
 * not yet target for any Apache SIS release at this time.</div>
 *
 * @author Marc Le Bihan
 * @version 0.5
 * @module
 * @since 0.5
 */
public class InputFeatureStream extends InputStream {

  private MappedByteReader reader;

  /**
   * SQL Statement executed.
   */
  private String sql;

  /**
   * Marks the end of file.
   */
  private boolean endOfFile;

  /**
   * Shapefile.
   */
  private File shapefile;

  /**
   * Database file.
   */
  private File databaseFile;

  /**
   * Shapefile index.
   */
  private File shapefileIndex;

  /**
   * Indicates that the shape file has a valid index provided with it.
   */
  private boolean hasShapefileIndex;

  /**
   * Type of the features contained in this shapefile.
   */
  private DefaultFeatureType featuresType;

  /**
   * Shapefile reader.
   */
  private ShapefileByteReader shapefileReader;

  /**
   * Create an input stream of features over a connection.
   *
   * @param shpfile      Shapefile.
   * @param dbaseFile    Database file.
   * @param shpfileIndex Shapefile index, null if none provided, will be checked for existence.
   * @param sqlStatement SQL Statement to run, if null, a SELECT * FROM DBF will occurs.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException      if the shapefile has not been found.
   * @throws DbaseFileNotFoundException      if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile, File shpfileIndex, String sqlStatement)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException {
    try {
      this.reader = new MappedByteReader(dbaseFile, null);

      if (sqlStatement == null) {
        this.sql = MessageFormat.format("SELECT * FROM {0}", dbaseFile.getName());
      } else {
        this.sql = sqlStatement;
      }

      this.shapefile = shpfile;
      this.databaseFile = dbaseFile;

      if (shpfileIndex != null && (shpfileIndex.exists() && shpfileIndex.isFile())) {
        this.shapefileIndex = shpfileIndex;
        this.hasShapefileIndex = true;
      } else {
        this.hasShapefileIndex = false;
      }

      this.shapefileReader = new ShapefileByteReader(this.shapefile, this.databaseFile, this.shapefileIndex);
      this.featuresType = this.shapefileReader.getFeaturesType();
    } catch (SQLInvalidDbaseFileFormatException ex) {
      // Promote this exception to an DataStoreException compatible exception.
      throw new InvalidDbaseFileFormatException(ex.getMessage(), ex);
    } catch (SQLDbaseFileNotFoundException ex) {
      // Promote this exception to an DataStoreException compatible exception.
      throw new DbaseFileNotFoundException(ex.getMessage(), ex);
    } catch (SQLShapefileNotFoundException ex) {
      // Promote this exception to an DataStoreException compatible exception.
      throw new ShapefileNotFoundException(ex.getMessage(), ex);
    }
  }

  /**
   * Create an input stream of features over a connection, responding to a SELECT * FROM DBF statement.
   *
   * @param shpfile      Shapefile.
   * @param dbaseFile    Database file.
   * @param shpfileIndex Shapefile index, null if none provided, will be checked for existence.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException      if the shapefile has not been found.
   * @throws DbaseFileNotFoundException      if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile, File shpfileIndex)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException {
    this(shpfile, dbaseFile, shpfileIndex, null);
  }

  /**
   * Create an input stream of features over a connection, responding to a SELECT * FROM DBF statement.
   *
   * @param shpfile   Shapefile.
   * @param dbaseFile Database file.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException      if the shapefile has not been found.
   * @throws DbaseFileNotFoundException      if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException {
    this(shpfile, dbaseFile, null);
  }

  /**
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() {
    throw new UnsupportedOperationException(
        "InputFeatureStream doesn't allow the use of read(). Use readFeature() instead.");
  }

  /**
   * @see java.io.InputStream#available()
   */
  @Override
  public int available() {
    throw new UnsupportedOperationException(
        "InputFeatureStream doesn't allow the use of available(). Use readFeature() will return null when feature are no more available.");
  }

  /**
   * @see java.io.InputStream#close()
   */
  @Override
  public void close() {
  }

  /**
   * Read next feature responding to the SQL query.
   *
   * @return Feature, null if no more feature is available.
   * @throws DataStoreClosedException        if the current connection used to query the shapefile has been closed.
   * @throws DataStoreQueryException         if the statement used to query the shapefile content is incorrect, or
   *                                         requires a shapefile index to be executed and none is available.
   * @throws DataStoreQueryResultException   if the shapefile results cause a trouble (wrong format, for example).
   * @throws InvalidShapefileFormatException if the shapefile structure shows a problem.
   */
  public AbstractFeature readFeature()
      throws DataStoreClosedException, DataStoreQueryException, InvalidShapefileFormatException {
    try {
      return internalReadFeature();
    } catch (SQLConnectionClosedException e) {
      throw new DataStoreClosedException(e.getMessage(), e);
    } catch (SQLFeatureNotSupportedException e) {
      throw new DataStoreQueryException(e.getMessage(), e);
    } catch (SQLNoDirectAccessAvailableException e) {
      throw new DataStoreQueryException(e.getMessage(), e);
    }
  }

  /**
   * Return the features type.
   *
   * @return Features type.
   */
  public DefaultFeatureType getFeaturesType() {
    return this.featuresType;
  }

  /**
   * Returns the shapefile descriptor.
   *
   * @return Shapefile descriptor.
   */
  public ShapefileDescriptor getShapefileDescriptor() {
    return this.shapefileReader.getShapefileDescriptor();
  }

  /**
   * Returns the database fields descriptors.
   *
   * @return List of fields descriptors.
   */
  public List<DBase3FieldDescriptor> getDatabaseFieldsDescriptors() {
    return this.shapefileReader.getFieldsDescriptors();
  }

  /**
   * Checks if the shapefile has an index provided with it.
   *
   * @return true if an index file (.shx) has been given with the shapefile.
   */
  public boolean hasShapefileIndex() {
    return this.hasShapefileIndex;
  }

  /**
   * Read next feature responding to the SQL query.
   *
   * @return Feature, null if no more feature is available.
   * @throws SQLConnectionClosedException          if the connection is closed.
   * @throws SQLFeatureNotSupportedException       if a SQL ability is not currently available through this driver.
   * @throws InvalidShapefileFormatException       if the shapefile format is invalid.
   * @throws SQLNoDirectAccessAvailableException   if the underlying SQL statement requires a direct access in the
   *                                               shapefile, but the shapefile cannot allow it.
   */
  private AbstractFeature internalReadFeature()
      throws SQLConnectionClosedException, SQLFeatureNotSupportedException, InvalidShapefileFormatException, SQLNoDirectAccessAvailableException {
    try {
      if (!this.reader.nextRowAvailable()) {
        return null;
      }
      AbstractFeature feature = (AbstractFeature) this.featuresType.newInstance();
      this.reader.readNextRowAsObjects();
      this.reader.loadRowIntoFeature(feature);
      this.shapefileReader.setRowNum(this.reader.getRowNum());
      this.shapefileReader.completeFeature(feature);
      return feature;
    } catch (SQLInvalidRecordNumberForDirectAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
