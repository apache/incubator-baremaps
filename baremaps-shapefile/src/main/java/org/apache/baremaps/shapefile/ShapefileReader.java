/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.shapefile;



import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Provides a ShapeFile Reader.
 *
 * <p>
 * <div class="warning">This is an experimental class, not yet target for any Apache SIS release at
 * this time.</div>
 *
 * @author Travis L. Pinney
 * @see <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">ESRI Shapefile
 *      Specification</a>
 * @see <a href="http://ulisse.elettra.trieste.it/services/doc/dbase/DBFstruct.htm">dBASE III File
 *      Structure</a>
 */
public class ShapefileReader {
  /** Shapefile. */
  private File shapefile;

  /** Database file. */
  private File databaseFile;

  /** Shapefile index, if any. */
  private File shapeFileIndex;

  /** Shapefile descriptor. */
  private ShapefileDescriptor shapefileDescriptor;

  /** Database field descriptors. */
  private List<DBaseFieldDescriptor> databaseFieldsDescriptors;

  /**
   * Construct a Shapefile from a file.
   *
   * @param shapefile file to read.
   */
  public ShapefileReader(String shapefile) {
    Objects.requireNonNull(shapefile, "The shapefile to load cannot be null.");

    this.shapefile = new File(shapefile);

    // Deduct database file name by suffixing it by dbf (trying to respect the same case).
    StringBuilder dbfFileName = new StringBuilder(shapefile);

    String dbfSuffix = null;
    dbfSuffix = shapefile.endsWith("shp") ? "dbf" : dbfSuffix;
    dbfSuffix = shapefile.endsWith("SHP") ? "DBF" : dbfSuffix;
    dbfSuffix = shapefile.endsWith("Shp") ? "Dbf" : dbfSuffix;
    dbfSuffix = (dbfSuffix == null) ? "dbf" : dbfSuffix;

    dbfFileName.replace(shapefile.length() - 3, shapefile.length(), dbfSuffix);
    this.databaseFile = new File(dbfFileName.toString());

    // Deduct shapefile index file name by suffixing it by shx (trying to respect the same case).
    StringBuilder shapeFileIndexName = new StringBuilder(shapefile);

    String shapeFileIndexSuffix = null;
    shapeFileIndexSuffix = shapefile.endsWith("shp") ? "shx" : shapeFileIndexSuffix;
    shapeFileIndexSuffix = shapefile.endsWith("SHP") ? "SHX" : shapeFileIndexSuffix;
    shapeFileIndexSuffix = shapefile.endsWith("Shp") ? "Shx" : shapeFileIndexSuffix;
    shapeFileIndexSuffix = (shapeFileIndexSuffix == null) ? "shx" : shapeFileIndexSuffix;

    shapeFileIndexName.replace(shapefile.length() - 3, shapefile.length(), shapeFileIndexSuffix);
    this.shapeFileIndex = new File(shapeFileIndexName.toString());
  }

  /**
   * Construct a Shapefile from a file.
   *
   * @param shpfile file to read.
   * @param dbasefile Associated DBase file.
   */
  public ShapefileReader(String shpfile, String dbasefile) {
    Objects.requireNonNull(shpfile, "The shapefile to load cannot be null.");
    Objects.requireNonNull(dbasefile, "The DBase III file to load cannot be null.");
    this.shapefile = new File(shpfile);
    this.databaseFile = new File(dbasefile);
  }

  /**
   * Construct a Shapefile from a file.
   *
   * @param shpfile file to read.
   * @param dbasefile Associated DBase file.
   * @param shpfileIndex Associated Shapefile index, may be null.
   */
  public ShapefileReader(String shpfile, String dbasefile, String shpfileIndex) {
    this(shpfile, dbasefile);
    this.shapeFileIndex = new File(shpfileIndex);
  }

  /**
   * Returns the shapefile descriptor.
   *
   * @return Shapefile descriptor.
   */
  public ShapefileDescriptor getShapefileDescriptor() {
    return this.shapefileDescriptor;
  }

  /**
   * Returns the database fields descriptors.
   *
   * @return List of fields descriptors.
   */
  public List<DBaseFieldDescriptor> getDatabaseFieldsDescriptors() {
    return this.databaseFieldsDescriptors;
  }

  /**
   * Returns the underlying DBase file used by this Shapefile.
   *
   * @return Dbase file.
   */
  public File getFileDatabase() {
    return this.databaseFile;
  }

  /**
   * Returns the shape file designed by this shapefile object.
   *
   * @return Shape file.
   */
  public File getFileShapefile() {
    return this.shapefile;
  }

  /**
   * Return the shapefile Index provided with the shapefile, if any.
   *
   * @return Shapefile Index file or null if none given.
   */
  public File getFileShapefileIndex() {
    return this.shapeFileIndex;
  }

  /**
   * Find features corresponding to an SQL request SELECT * FROM database.
   *
   * @return Features
   */
  public ShapefileInputStream read() throws IOException {
    ShapefileInputStream is =
        new ShapefileInputStream(this.shapefile, this.databaseFile, this.shapeFileIndex);
    this.shapefileDescriptor = is.getShapefileDescriptor();
    this.databaseFieldsDescriptors = is.getDatabaseFieldsDescriptors();
    return is;
  }

  /**
   * Load shapefile descriptors : features types, shapefileDescriptor, database field descriptors :
   * this is also automatically done when executing a query on it, by findAll.
   */
  public void loadDescriptors() throws IOException {
    try (ShapefileInputStream is = read()) {
      // Doing a read is sufficient to initialize the internal descriptors.
    }
  }
}
