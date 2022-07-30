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
import java.util.List;
import java.util.Objects;

import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.internal.shapefile.ShapefileDescriptor;
import org.apache.sis.internal.shapefile.jdbc.DBase3FieldDescriptor;

/**
 * Provides a ShapeFile Reader.
 *
 * <div class="warning">This is an experimental class,
 * not yet target for any Apache SIS release at this time.</div>
 *
 * @author  Travis L. Pinney
 * @version 0.5
 *
 * @see <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">ESRI Shapefile Specification</a>
 * @see <a href="http://ulisse.elettra.trieste.it/services/doc/dbase/DBFstruct.htm">dBASE III File Structure</a>
 *
 * @since 0.5
 * @module
 */
public class ShapeFile {
    /** Shapefile. */
    private File shapeFile;

    /** Database file. */
    private File databaseFile;

    /** Shapefile index, if any. */
    private File shapeFileIndex;

    /** Type of the features contained in this shapefile. */
    private DefaultFeatureType featuresType;

    /** Shapefile descriptor. */
    private ShapefileDescriptor shapefileDescriptor;

    /** Database field descriptors. */
    private List<DBase3FieldDescriptor> databaseFieldsDescriptors;

    /**
     * Construct a Shapefile from a file.
     * @param shpfile file to read.
     */
    public ShapeFile(String shpfile) {
        Objects.requireNonNull(shpfile, "The shapefile to load cannot be null.");

        this.shapeFile = new File(shpfile);

        // Deduct database file name by suffixing it by dbf (trying to respect the same case).
        StringBuilder dbfFileName = new StringBuilder(shpfile);

        String dbfSuffix = null;
        dbfSuffix = shpfile.endsWith("shp") ? "dbf" : dbfSuffix;
        dbfSuffix = shpfile.endsWith("SHP") ? "DBF" : dbfSuffix;
        dbfSuffix = shpfile.endsWith("Shp") ? "Dbf" : dbfSuffix;
        dbfSuffix = (dbfSuffix == null) ? "dbf" : dbfSuffix;

        dbfFileName.replace(shpfile.length() - 3, shpfile.length(), dbfSuffix);
        this.databaseFile = new File(dbfFileName.toString());

        // Deduct shapefile index file name by suffixing it by shx (trying to respect the same case).
        StringBuilder shapeFileIndexName = new StringBuilder(shpfile);

        String shapeFileIndexSuffix = null;
        shapeFileIndexSuffix = shpfile.endsWith("shp") ? "shx" : shapeFileIndexSuffix;
        shapeFileIndexSuffix = shpfile.endsWith("SHP") ? "SHX" : shapeFileIndexSuffix;
        shapeFileIndexSuffix = shpfile.endsWith("Shp") ? "Shx" : shapeFileIndexSuffix;
        shapeFileIndexSuffix = (shapeFileIndexSuffix == null) ? "shx" : shapeFileIndexSuffix;

        shapeFileIndexName.replace(shpfile.length() - 3, shpfile.length(), shapeFileIndexSuffix);
        this.shapeFileIndex = new File(shapeFileIndexName.toString());
    }

    /**
     * Construct a Shapefile from a file.
     * @param shpfile file to read.
     * @param dbasefile Associated DBase file.
     */
    public ShapeFile(String shpfile, String dbasefile) {
        Objects.requireNonNull(shpfile, "The shapefile to load cannot be null.");
        Objects.requireNonNull(dbasefile, "The DBase III file to load cannot be null.");

        this.shapeFile = new File(shpfile);
        this.databaseFile = new File(dbasefile);
    }

    /**
     * Construct a Shapefile from a file.
     * @param shpfile file to read.
     * @param dbasefile Associated DBase file.
     * @param shpfileIndex Associated Shapefile index, may be null.
     */
    public ShapeFile(String shpfile, String dbasefile, String shpfileIndex) {
        this(shpfile, dbasefile);
        this.shapeFileIndex = new File(shpfileIndex);
    }

    /**
     * Return the default feature type.
     * @return Feature type.
     */
    public DefaultFeatureType getFeaturesType() {
        return this.featuresType;
    }

    /**
     * Returns the shapefile descriptor.
     * @return Shapefile descriptor.
     */
    public ShapefileDescriptor getShapefileDescriptor() {
        return this.shapefileDescriptor;
    }

    /**
     * Returns the database fields descriptors.
     * @return List of fields descriptors.
     */
    public List<DBase3FieldDescriptor> getDatabaseFieldsDescriptors() {
        return this.databaseFieldsDescriptors;
    }

    /**
     * Returns the underlying DBase file used by this Shapefile.
     * @return Dbase file.
     */
    public File getFileDatabase() {
        return this.databaseFile;
    }

    /**
     * Returns the shape file designed by this shapefile object.
     * @return Shape file.
     */
    public File getFileShapefile() {
        return this.shapeFile;
    }

    /**
     * Return the shapefile Index provided with the shapefile, if any.
     * @return Shapefile Index file or null if none given.
     */
    public File getFileShapefileIndex() {
        return this.shapeFileIndex;
    }

    /**
     * Find features corresponding to an SQL request SELECT * FROM database.
     * @return Features
     * @throws DbaseFileNotFoundException if the database file has not been found.
     * @throws ShapefileNotFoundException if the shapefile has not been found.
     * @throws InvalidDbaseFileFormatException if the database file format is invalid.
     * @throws InvalidShapefileFormatException if the shapefile format is invalid.
     */
    public InputFeatureStream findAll() throws InvalidDbaseFileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException, InvalidShapefileFormatException {
        return find(null);
    }

    /**
     * Find features corresponding to an SQL request SELECT * FROM database.
     * @param sqlStatement SQL Statement to run, if null, will default to SELECT * FROM database.
     * @return Features
     * @throws DbaseFileNotFoundException if the database file has not been found.
     * @throws ShapefileNotFoundException if the shapefile has not been found.
     * @throws InvalidDbaseFileFormatException if the database file format is invalid.
     * @throws InvalidShapefileFormatException if the shapefile format is invalid.
     */
    public InputFeatureStream find(String sqlStatement) throws InvalidDbaseFileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException, InvalidShapefileFormatException {
        InputFeatureStream is = new InputFeatureStream(this.shapeFile, this.databaseFile, this.shapeFileIndex, sqlStatement);
        this.featuresType = is.getFeaturesType();
        this.shapefileDescriptor = is.getShapefileDescriptor();
        this.databaseFieldsDescriptors = is.getDatabaseFieldsDescriptors();
        return is;
    }

    /**
     * Load shapefile descriptors : features types, shapefileDescriptor, database field descriptors :
     * this is also automatically done when executing a query on it, by findAll.
     * @throws DbaseFileNotFoundException if the database file has not been found.
     * @throws ShapefileNotFoundException if the shapefile has not been found.
     * @throws InvalidDbaseFileFormatException if the database file format is invalid.
     * @throws InvalidShapefileFormatException if the shapefile format is invalid.
     */
    public void loadDescriptors() throws InvalidDbaseFileFormatException, InvalidShapefileFormatException, ShapefileNotFoundException, DbaseFileNotFoundException {
        // Doing an simple query will init the internal descriptors.
        // It prepares a SELECT * FROM <DBase> but don't read a record by itself.
        try(InputFeatureStream is = findAll()) {
        }
    }
}
