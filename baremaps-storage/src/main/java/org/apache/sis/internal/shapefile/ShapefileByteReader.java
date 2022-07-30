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

import java.io.*;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.*;

import org.apache.sis.feature.DefaultAttributeType;
import org.apache.sis.feature.DefaultFeatureType;
import org.apache.sis.storage.shapefile.InvalidShapefileFormatException;
import org.apache.sis.storage.shapefile.ShapeTypeEnum;
import org.apache.sis.feature.AbstractFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Reader of a Shapefile Binary content by the way of a {@link java.nio.MappedByteBuffer}
 *
 * @author Marc Le Bihan
 * @version 0.5
 * @module
 * @since 0.5
 */
public class ShapefileByteReader extends
    CommonByteReader<InvalidShapefileFormatException, SQLShapefileNotFoundException> {

  /**
   * Name of the Geometry field.
   */
  private static final String GEOMETRY_NAME = "geometry";

  /**
   * Shapefile descriptor.
   */
  private ShapefileDescriptor shapefileDescriptor;

  /**
   * Database Field descriptors.
   */
  private List<DBase3FieldDescriptor> databaseFieldsDescriptors;

  /**
   * Type of the features contained in this shapefile.
   */
  private DefaultFeatureType featuresType;

  /**
   * Shapefile index.
   */
  private File shapeFileIndex;

  /**
   * Shapefile indexes (loaded from .SHX file, if any found).
   */
  private ArrayList<Integer> indexes;

  /**
   * Shapefile records lengths (loaded from .SHX file, if any found).
   */
  private ArrayList<Integer> recordsLengths;

  private GeometryFactory geometryFactory = new GeometryFactory();

  /**
   * Construct a shapefile byte reader.
   *
   * @param shapefile      Shapefile.
   * @param dbaseFile      underlying database file name.
   * @param shapefileIndex Shapefile index, if any. Null else.
   * @throws InvalidShapefileFormatException    if the shapefile format is invalid.
   * @throws SQLInvalidDbaseFileFormatException if the database file format is invalid.
   * @throws SQLShapefileNotFoundException      if the shapefile has not been found.
   * @throws SQLDbaseFileNotFoundException      if the database file has not been found.
   */
  public ShapefileByteReader(File shapefile, File dbaseFile, File shapefileIndex)
      throws InvalidShapefileFormatException, SQLInvalidDbaseFileFormatException, SQLShapefileNotFoundException, SQLDbaseFileNotFoundException {
    super(shapefile, InvalidShapefileFormatException.class, SQLShapefileNotFoundException.class);
    this.shapeFileIndex = shapefileIndex;

    loadDatabaseFieldDescriptors(dbaseFile);
    loadDescriptor();

    if (this.shapeFileIndex != null) {
      loadShapefileIndexes();
    }

    this.featuresType = getFeatureType(shapefile.getName());
  }

  /**
   * Returns the DBase 3 fields descriptors.
   *
   * @return Fields descriptors.
   */
  public List<DBase3FieldDescriptor> getFieldsDescriptors() {
    return this.databaseFieldsDescriptors;
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
   * Returns the type of the features contained in this shapefile.
   *
   * @return Features type.
   */
  public DefaultFeatureType getFeaturesType() {
    return this.featuresType;
  }

  /**
   * Create a feature descriptor.
   *
   * @param name Name of the field.
   * @return The feature type.
   */
  private DefaultFeatureType getFeatureType(final String name) {
    Objects.requireNonNull(name, "The feature name cannot be null.");

    final int n = this.databaseFieldsDescriptors.size();
    final DefaultAttributeType<?>[] attributes = new DefaultAttributeType<?>[n + 1];
    final Map<String, Object> properties = new HashMap<>(4);

    // Load data field.
    for (int i = 0; i < n; i++) {
      var fieldDescriptor = this.databaseFieldsDescriptors.get(i);
      properties.put(DefaultAttributeType.NAME_KEY, fieldDescriptor.getName());

      // TODO: move somewhere else
      Class type = switch (fieldDescriptor.getType()) {
        case Character -> String.class;
        case Number -> fieldDescriptor.getDecimalCount() == 0 ? Long.class: Double.class;
        case Currency -> Double.class;
        case Integer -> Integer.class;
        case Double -> Double.class;
        case AutoIncrement -> Integer.class;
        case Logical -> String.class;
        case Date -> String.class;
        case Memo -> String.class;
        case FloatingPoint -> String.class;
        case Picture -> String.class;
        case VariField -> String.class;
        case Variant -> String.class;
        case TimeStamp -> String.class;
        case DateTime -> String.class;
      };

      attributes[i] = new DefaultAttributeType<>(properties, type, 1, 1, null);
    }

    // Add geometry field.
    properties.put(DefaultAttributeType.NAME_KEY, GEOMETRY_NAME);
    attributes[n] = new DefaultAttributeType<>(properties, Geometry.class, 1, 1, null);

    // Add name.
    properties.put(DefaultAttributeType.NAME_KEY, name);
    return new DefaultFeatureType(properties, false, null, attributes);
  }

  /**
   * Load shapefile descriptor.
   */
  private void loadDescriptor() {
    this.shapefileDescriptor = new ShapefileDescriptor(getByteBuffer());
  }

  /**
   * Load shapefile indexes.
   *
   * @return true if shapefile indexes has been read, false if none where available or a problem occured.
   */
  private boolean loadShapefileIndexes() {
    if (this.shapeFileIndex == null) {
      return false;
    }

    try (FileInputStream fis = new FileInputStream(this.shapeFileIndex); FileChannel fc = fis.getChannel()) {
      try {
        int fsize = (int) fc.size();
        MappedByteBuffer indexesByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fsize);

        // Indexes entries follow.
        this.indexes = new ArrayList<>();
        this.recordsLengths = new ArrayList<>();
        indexesByteBuffer.position(100);
        indexesByteBuffer.order(ByteOrder.BIG_ENDIAN);

        while (indexesByteBuffer.hasRemaining()) {
          this.indexes.add(
              indexesByteBuffer.getInt());        // Data offset : the position of the record in the main shapefile, expressed in words (16 bits).
          this.recordsLengths.add(indexesByteBuffer.getInt()); // Length of this shapefile record.
        }
        return true;
      } catch (IOException e) {
        this.shapeFileIndex = null;
        return false;
      }
    } catch (FileNotFoundException e) {
      this.shapeFileIndex = null;
      return false;
    } catch (IOException e) {
      this.shapeFileIndex = null;
      return false;
    }
  }

  /**
   * Load database field descriptors.
   *
   * @param dbaseFile Database file.
   * @throws SQLInvalidDbaseFileFormatException if the database format is incorrect.
   * @throws SQLDbaseFileNotFoundException      if the database file cannot be found.
   */
  private void loadDatabaseFieldDescriptors(File dbaseFile)
      throws SQLInvalidDbaseFileFormatException, SQLDbaseFileNotFoundException {
    MappedByteReader databaseReader = null;

    try {
      databaseReader = new MappedByteReader(dbaseFile, null);
      this.databaseFieldsDescriptors = databaseReader.getFieldsDescriptors();
    } finally {
      if (databaseReader != null) {
        try {
          databaseReader.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Direct access to a feature by its record number.
   *
   * @param recordNumber Record number.
   * @throws SQLNoDirectAccessAvailableException            if this shape file doesn't allow direct acces, because it
   *                                                        has no index.
   * @throws SQLInvalidRecordNumberForDirectAccessException if the record number asked for is invalid (below the start,
   *                                                        after the end).
   */
  public void setRowNum(int recordNumber)
      throws SQLNoDirectAccessAvailableException, SQLInvalidRecordNumberForDirectAccessException {
    // Check that the asked record number is not before the first.
    if (recordNumber < 1) {
      throw new SQLInvalidRecordNumberForDirectAccessException(recordNumber, "Wrong direct access before start");
    }

    // Check that the shapefile allows direct access : it won't if it has no index.
    if (this.shapeFileIndex == null) {
      throw new SQLNoDirectAccessAvailableException("No direct access");
    }

    int position = this.indexes.get(recordNumber - 1) * 2; // Indexes unit are words (16 bits).

    // Check that the asked record number is not after the last.
    if (position >= this.getByteBuffer().capacity()) {
      throw new SQLInvalidRecordNumberForDirectAccessException(recordNumber, "Wrong direct access after last");
    }

    try {
      getByteBuffer().position(position);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Wrong position", e);
    }
  }

  /**
   * Complete a feature with shapefile content.
   *
   * @param feature Feature to complete.
   * @throws InvalidShapefileFormatException if a validation problem occurs.
   */
  public void completeFeature(AbstractFeature feature) throws InvalidShapefileFormatException {
    // insert points into some type of list
    int RecordNumber = getByteBuffer().getInt();
    @SuppressWarnings("unused")
    int ContentLength = getByteBuffer().getInt();

    getByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
    int iShapeType = getByteBuffer().getInt();

    ShapeTypeEnum type = ShapeTypeEnum.get(iShapeType);

    if (type == null) {
      throw new InvalidShapefileFormatException(
          MessageFormat.format("The shapefile feature type {0} doesn''t match to any known feature type.",
              this.featuresType));
    }

    switch (type) {
      case Point:
        loadPointFeature(feature);
        break;

      case Polygon:
        loadPolygonFeature(feature);
        break;

      case PolyLine:
        loadPolylineFeature(feature);
        break;

      default:
        throw new InvalidShapefileFormatException("Unsupported shapefile type: " + iShapeType);
    }

    getByteBuffer().order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Load point feature.
   *
   * @param feature Feature to fill.
   */
  private void loadPointFeature(AbstractFeature feature) {
    double x = getByteBuffer().getDouble();
    double y = getByteBuffer().getDouble();
    Point pnt = geometryFactory.createPoint(new Coordinate(x, y));
    feature.setPropertyValue(GEOMETRY_NAME, pnt);
  }

  /**
   * Load polygon feature.
   *
   * @param feature Feature to fill.
   */
  private void loadPolygonFeature(AbstractFeature feature) {
    /* double xmin = */
    getByteBuffer().getDouble();
    /* double ymin = */
    getByteBuffer().getDouble();
    /* double xmax = */
    getByteBuffer().getDouble();
    /* double ymax = */
    getByteBuffer().getDouble();
    int numParts = getByteBuffer().getInt();
    int numPoints = getByteBuffer().getInt();

    Polygon poly;

    // Handle multiple polygon parts.
    if (numParts > 1) {
      poly = readMultiplePolygonParts(numParts, numPoints);
    } else {
      // Polygon with an unique part.
      poly = readUniquePolygonPart(numPoints);
    }

    feature.setPropertyValue(GEOMETRY_NAME, poly);
  }

  /**
   * Read a polygon that has a unique part.
   *
   * @param numPoints Number of the points of the polygon.
   * @return Polygon.
   */
  @Deprecated
  // As soon as the readMultiplePolygonParts method proofs working well, this readUniquePolygonPart method can be removed and all calls be deferred to readMultiplePolygonParts.
  private Polygon readUniquePolygonPart(int numPoints) {
    /*int part = */
    getByteBuffer().getInt();

    var coordinates = new CoordinateList();

    // create a line from the points
    double xpnt = getByteBuffer().getDouble();
    double ypnt = getByteBuffer().getDouble();

    coordinates.add(new Coordinate(xpnt, ypnt));

    for (int j = 0; j < numPoints - 1; j++) {
      xpnt = getByteBuffer().getDouble();
      ypnt = getByteBuffer().getDouble();
      coordinates.add(new Coordinate(xpnt, ypnt));
    }

    return geometryFactory.createPolygon(coordinates.toCoordinateArray());
  }

  /**
   * Read a polygon that has multiple parts.
   *
   * @param numParts  Number of parts of this polygon.
   * @param numPoints Total number of points of this polygon, all parts considered.
   * @return a multiple part polygon.
   */
  private Polygon readMultiplePolygonParts(int numParts, int numPoints) {
    /**
     * From ESRI Specification :
     * Parts : 0 5  (meaning : 0 designs the first v1, 5 designs the first v5 on the points list below).
     * Points : v1 v2 v3 v4 v1 v5 v8 v7 v6 v5
     *
     * POSITION  FIELD       VALUE      TYPE      NUMBER     ORDER
     * Byte 0    Shape Type  5          Integer   1          Little
     * Byte 4    Box         Box        Double    4          Little
     * Byte 36   NumParts    NumParts   Integer   1          Little
     * Byte 40   NumPoints   NumPoints  Integer   1          Little
     * Byte 44   Parts       Parts      Integer   NumParts   Little
     * Byte X    Points      Points     Point     NumPoints  Little
     */
    int[] partsIndexes = new int[numParts];

    // Read all the parts indexes (starting at byte 44).
    for (int index = 0; index < numParts; index++) {
      partsIndexes[index] = getByteBuffer().getInt();
    }

    // Read all the points.
    double[] xPoints = new double[numPoints];
    double[] yPoints = new double[numPoints];

    for (int index = 0; index < numPoints; index++) {
      xPoints[index] = getByteBuffer().getDouble();
      yPoints[index] = getByteBuffer().getDouble();
    }

    // Create the polygon from the points.
    var coordinates = new CoordinateList();

    // create a line from the points
    for (int index = 0; index < numPoints; index++) {
      // Check if this index is one that begins a new part.
      boolean newPolygon = false;

      for (int j = 0; j < partsIndexes.length; j++) {
        if (partsIndexes[j] == index) {
          newPolygon = true;
          break;
        }
      }

      if (newPolygon) {
        coordinates.add(new Coordinate(xPoints[index], yPoints[index]));
      } else {
        coordinates.add(new Coordinate(xPoints[index], yPoints[index]));
      }
    }

    return geometryFactory.createPolygon(coordinates.toCoordinateArray());
  }

  /**
   * Load polyline feature.
   *
   * @param feature Feature to fill.
   */
  private void loadPolylineFeature(AbstractFeature feature) {
    /* double xmin = */
    getByteBuffer().getDouble();
    /* double ymin = */
    getByteBuffer().getDouble();
    /* double xmax = */
    getByteBuffer().getDouble();
    /* double ymax = */
    getByteBuffer().getDouble();

    int NumParts = getByteBuffer().getInt();
    int NumPoints = getByteBuffer().getInt();

    int[] NumPartArr = new int[NumParts + 1];

    for (int n = 0; n < NumParts; n++) {
      int idx = getByteBuffer().getInt();
      NumPartArr[n] = idx;
    }
    NumPartArr[NumParts] = NumPoints;

    double xpnt, ypnt;
    var coordinates = new CoordinateList();

    for (int m = 0; m < NumParts; m++) {
      xpnt = getByteBuffer().getDouble();
      ypnt = getByteBuffer().getDouble();
      coordinates.add(new Coordinate(xpnt, ypnt));

      for (int j = NumPartArr[m]; j < NumPartArr[m + 1] - 1; j++) {
        xpnt = getByteBuffer().getDouble();
        ypnt = getByteBuffer().getDouble();
        coordinates.add(new Coordinate(xpnt, ypnt));
      }
    }

    feature.setPropertyValue(GEOMETRY_NAME, geometryFactory.createLineString(coordinates.toCoordinateArray()));
  }
}
