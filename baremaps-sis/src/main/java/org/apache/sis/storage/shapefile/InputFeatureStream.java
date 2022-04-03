/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.sis.storage.shapefile;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLFeatureNotSupportedException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.*;
import org.apache.sis.internal.shapefile.jdbc.*;
import org.apache.sis.internal.shapefile.jdbc.connection.DBFConnection;
import org.apache.sis.internal.shapefile.jdbc.metadata.DBFDatabaseMetaData;
import org.apache.sis.internal.shapefile.jdbc.resultset.*;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLIllegalParameterException;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLInvalidStatementException;
import org.apache.sis.internal.shapefile.jdbc.sql.SQLUnsupportedParsingFeatureException;
import org.apache.sis.internal.shapefile.jdbc.statement.DBFStatement;
import org.apache.sis.internal.system.Modules;
import org.apache.sis.storage.DataStoreClosedException;
import org.apache.sis.util.logging.Logging;

/**
 * Input Stream of features.
 *
 * <p><div class="warning">This is an experimental class, not yet target for any Apache SIS release
 * at this time.</div>
 *
 * @author Marc Le Bihan
 * @version 0.5
 * @since 0.5
 * @module
 */
public class InputFeatureStream extends InputStream {
  /** Logger. */
  private static final Logger LOGGER = Logging.getLogger(Modules.SHAPEFILE);

  /** Resource bundle. */
  private ResourceBundle rsc = ResourceBundle.getBundle(InputFeatureStream.class.getName());

  /** Dedicated connection to DBF. */
  private DBFConnection connection;

  /** Statement. */
  private DBFStatement stmt;

  /** ResultSet. */
  private DBFRecordBasedResultSet rs;

  /** SQL Statement executed. */
  private String sql;

  /** Marks the end of file. */
  private boolean endOfFile;

  /** Shapefile. */
  private File shapefile;

  /** Database file. */
  private File databaseFile;

  /** Shapefile index. */
  private File shapefileIndex;

  /** Indicates that the shape file has a valid index provided with it. */
  private boolean hasShapefileIndex;

  /** Type of the features contained in this shapefile. */
  private DefaultFeatureType featuresType;

  /** Shapefile reader. */
  private ShapefileByteReader shapefileReader;

  /**
   * Create an input stream of features over a connection.
   *
   * @param shpfile Shapefile.
   * @param dbaseFile Database file.
   * @param shpfileIndex Shapefile index, null if none provided, will be checked for existence.
   * @param sqlStatement SQL Statement to run, if null, a SELECT * FROM DBF will occurs.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException if the shapefile has not been found.
   * @throws DbaseFileNotFoundException if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile, File shpfileIndex, String sqlStatement)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException,
          ShapefileNotFoundException, DbaseFileNotFoundException {
    try {
      this.connection = (DBFConnection) new DBFDriver().connect(dbaseFile.getAbsolutePath(), null);

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

      this.shapefileReader =
          new ShapefileByteReader(this.shapefile, this.databaseFile, this.shapefileIndex);
      this.featuresType = this.shapefileReader.getFeaturesType();

      try {
        executeQuery();
      } catch (SQLConnectionClosedException e) {
        // This would be an internal trouble because in this function (at least) it should be open.
        throw new RuntimeException(e.getMessage(), e);
      } catch (SQLInvalidStatementException e) {
        // This would be an internal trouble because if any SQL statement is executed for the dbase
        // file initialization, it should has a correct syntax or grammar.
        throw new RuntimeException(e.getMessage(), e);
      }
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
   * Create an input stream of features over a connection, responding to a SELECT * FROM DBF
   * statement.
   *
   * @param shpfile Shapefile.
   * @param dbaseFile Database file.
   * @param shpfileIndex Shapefile index, null if none provided, will be checked for existence.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException if the shapefile has not been found.
   * @throws DbaseFileNotFoundException if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile, File shpfileIndex)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException,
          ShapefileNotFoundException, DbaseFileNotFoundException {
    this(shpfile, dbaseFile, shpfileIndex, null);
  }

  /**
   * Create an input stream of features over a connection, responding to a SELECT * FROM DBF
   * statement.
   *
   * @param shpfile Shapefile.
   * @param dbaseFile Database file.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws InvalidDbaseFileFormatException if the Dbase file format is invalid.
   * @throws ShapefileNotFoundException if the shapefile has not been found.
   * @throws DbaseFileNotFoundException if the database file has not been found.
   */
  public InputFeatureStream(File shpfile, File dbaseFile)
      throws InvalidDbaseFileFormatException, InvalidShapefileFormatException,
          ShapefileNotFoundException, DbaseFileNotFoundException {
    this(shpfile, dbaseFile, null);
  }

  /** @see java.io.InputStream#read() */
  @Override
  public int read() {
    throw new UnsupportedOperationException(
        "InputFeatureStream doesn't allow the use of read(). Use readFeature() instead.");
  }

  /** @see java.io.InputStream#available() */
  @Override
  public int available() {
    throw new UnsupportedOperationException(
        "InputFeatureStream doesn't allow the use of available(). Use readFeature() will return null when feature are no more available.");
  }

  /** @see java.io.InputStream#close() */
  @Override
  public void close() {
    this.rs.close();
    this.stmt.close();
    this.connection.close();
  }

  /**
   * Read next feature responding to the SQL query.
   *
   * @return Feature, null if no more feature is available.
   * @throws DataStoreClosedException if the current connection used to query the shapefile has been
   *     closed.
   * @throws DataStoreQueryException if the statement used to query the shapefile content is
   *     incorrect, or requires a shapefile index to be executed and none is available.
   * @throws DataStoreQueryResultException if the shapefile results cause a trouble (wrong format,
   *     for example).
   * @throws InvalidShapefileFormatException if the shapefile structure shows a problem.
   */
  public AbstractFeature readFeature()
      throws DataStoreClosedException, DataStoreQueryException, DataStoreQueryResultException,
          InvalidShapefileFormatException {
    try {
      return internalReadFeature();
    } catch (SQLConnectionClosedException e) {
      throw new DataStoreClosedException(e.getMessage(), e);
    } catch (SQLInvalidStatementException
        | SQLIllegalParameterException
        | SQLNoSuchFieldException
        | SQLUnsupportedParsingFeatureException
        | SQLFeatureNotSupportedException e) {
      throw new DataStoreQueryException(e.getMessage(), e);
    } catch (SQLNotNumericException | SQLNotDateException e) {
      throw new DataStoreQueryResultException(e.getMessage(), e);
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
   * @throws SQLNotNumericException if a field expected numeric isn't.
   * @throws SQLNotDateException if a field expected of date kind, isn't.
   * @throws SQLNoSuchFieldException if a field doesn't exist.
   * @throws SQLIllegalParameterException if a parameter is illegal in the query.
   * @throws SQLInvalidStatementException if the SQL statement is invalid.
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLUnsupportedParsingFeatureException if a SQL ability is not currently available
   *     through this driver.
   * @throws SQLFeatureNotSupportedException if a SQL ability is not currently available through
   *     this driver.
   * @throws InvalidShapefileFormatException if the shapefile format is invalid.
   * @throws SQLNoDirectAccessAvailableException if the underlying SQL statement requires a direct
   *     access in the shapefile, but the shapefile cannot allow it.
   */
  private AbstractFeature internalReadFeature()
      throws SQLConnectionClosedException, SQLInvalidStatementException,
          SQLIllegalParameterException, SQLNoSuchFieldException,
          SQLUnsupportedParsingFeatureException, SQLNotNumericException, SQLNotDateException,
          SQLFeatureNotSupportedException, InvalidShapefileFormatException,
          SQLNoDirectAccessAvailableException {
    try {
      if (this.endOfFile) {
        return null;
      }

      int previousRecordNumber = this.rs.getRowNum();

      if (this.rs.next() == false) {
        this.endOfFile = true;
        return null;
      }

      int currentRecordNumber = this.rs.getRowNum();

      // On the shapefile, only jump in another place if a direct access is needed.
      boolean directAccesRequired = currentRecordNumber != (previousRecordNumber + 1);

      if (directAccesRequired) {
        try {
          if (LOGGER.isLoggable(Level.FINER)) {
            MessageFormat format =
                new MessageFormat(this.rsc.getString("log.shapefile_reading_with_direct_access"));
            LOGGER.finer(format.format(new Object[] {previousRecordNumber, currentRecordNumber}));
          }

          this.shapefileReader.setRowNum(currentRecordNumber);
        } catch (SQLInvalidRecordNumberForDirectAccessException e) {
          // This would be an internal API problem, because as soon as we handle a shapefile index,
          // we shall go through its relative shape feature file correctly.
          throw new RuntimeException(e.getMessage(), e);
        }
      } else {
        if (LOGGER.isLoggable(Level.FINER)) {
          MessageFormat format =
              new MessageFormat(this.rsc.getString("log.shapefile_reading_with_sequential_access"));
          LOGGER.finer(format.format(new Object[] {previousRecordNumber, currentRecordNumber}));
        }
      }

      AbstractFeature feature = this.featuresType.newInstance();
      this.shapefileReader.completeFeature(feature);
      DBFDatabaseMetaData metadata = (DBFDatabaseMetaData) this.connection.getMetaData();

      try (DBFBuiltInMemoryResultSetForColumnsListing rsDatabase =
          (DBFBuiltInMemoryResultSetForColumnsListing)
              metadata.getColumns(null, null, null, null)) {
        while (rsDatabase.next()) {
          String fieldName = rsDatabase.getString("COLUMN_NAME");
          Object fieldValue = this.rs.getObject(fieldName);

          // FIXME To allow features to be filled again, the values are converted to String again :
          // feature should allow any kind of data.
          String stringValue;

          if (fieldValue == null) {
            stringValue = null;
          } else {
            if (fieldValue instanceof Integer || fieldValue instanceof Long) {
              stringValue =
                  MessageFormat.format("{0,number,#0}", fieldValue); // Avoid thousand separator.
            } else {
              if (fieldValue instanceof Double || fieldValue instanceof Float) {
                // Avoid thousand separator.
                DecimalFormat df = new DecimalFormat();
                df.setGroupingUsed(false);
                stringValue = df.format(fieldValue);
              } else stringValue = fieldValue.toString();
            }
          }

          feature.setPropertyValue(fieldName, stringValue);
        }

        return feature;
      } catch (SQLNoResultException e) {
        // This an internal trouble, if it occurs.
        throw new RuntimeException(e.getMessage(), e);
      }
    } catch (SQLNoResultException e) {
      // We are trying to prevent this. If it occurs, we have an internal problem.
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Execute the wished SQL query.
   *
   * @throws SQLConnectionClosedException if the connection is closed.
   * @throws SQLInvalidStatementException if the given SQL Statement is invalid.
   */
  private void executeQuery() throws SQLConnectionClosedException, SQLInvalidStatementException {
    this.stmt = (DBFStatement) this.connection.createStatement();
    this.rs = (DBFRecordBasedResultSet) this.stmt.executeQuery(this.sql);
  }
}
