/*
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

package org.apache.baremaps.storage.geopackage;


import java.util.*;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.storage.*;
import org.locationtech.jts.geom.*;

/**
 * A table that stores rows in a GeoPackage table.
 */
public class GeoPackageTable extends AbstractDataCollection<Row> implements Table {

  private final FeatureDao featureDao;

  private final Schema schema;

  private final GeometryFactory geometryFactory;

  /**
   * Constructs a table from a feature DAO.
   *
   * @param featureDao the feature DAO
   */
  public GeoPackageTable(FeatureDao featureDao) {
    this.featureDao = featureDao;
    var name = featureDao.getTableName();
    var columns = new ArrayList<Column>();
    for (FeatureColumn column : featureDao.getColumns()) {
      var propertyName = column.getName();
      var propertyType = classType(column);
      columns.add(new ColumnImpl(propertyName, propertyType));
    }
    schema = new SchemaImpl(name, columns);
    geometryFactory = new GeometryFactory(new PrecisionModel(), (int) featureDao.getSrs().getId());
  }

  protected Class<?> classType(FeatureColumn column) {
    if (column.isGeometry()) {
      return Geometry.class;
    } else {
      return column.getDataType().getClassType();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Row> iterator() {
    return new GeopackageIterator(featureDao.queryForAll(), schema);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return featureDao.count();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Schema schema() {
    return schema;
  }

  /**
   * Converts a GeoPackage value to a Java value.
   *
   * @param value the GeoPackage value
   * @return the Java value
   */
  private Object asJavaValue(Object value) {
    if (value instanceof GeoPackageGeometryData geometry) {
      return asJtsGeometry(geometry.getGeometry());
    } else if (value instanceof Date date) {
      return value.toString();
    } else {
      return value;
    }
  }

  /**
   * Converts a GeoPackage geometry to a JTS geometry.
   *
   * @param geometry the GeoPackage geometry
   * @return the JTS geometry
   */
  private Geometry asJtsGeometry(mil.nga.sf.Geometry geometry) {
    if (geometry instanceof mil.nga.sf.Point point) {
      return asJtsPoint(point);
    } else if (geometry instanceof mil.nga.sf.LineString lineString) {
      return asJtsLineString(lineString);
    } else if (geometry instanceof mil.nga.sf.Polygon polygon) {
      return asJtsPolygon(polygon);
    } else if (geometry instanceof mil.nga.sf.MultiPoint multiPoint) {
      return asJtsMultiPoint(multiPoint);
    } else if (geometry instanceof mil.nga.sf.MultiLineString multiLineString) {
      return asJtsMultiLineString(multiLineString);
    } else if (geometry instanceof mil.nga.sf.MultiPolygon multiPolygon) {
      return asJtsMultiPolygon(multiPolygon);
    } else if (geometry instanceof mil.nga.sf.GeometryCollection geometryCollection) {
      return asJstGeometryCollection(geometryCollection);
    } else {
      // Unknown geometries are discarded
      return null;
    }
  }

  /**
   * Converts a GeoPackage geometry collection to a JTS geometry collection.
   *
   * @param geometryCollection the GeoPackage geometry collection
   * @return the JTS geometry collection
   */
  private GeometryCollection asJstGeometryCollection(
      mil.nga.sf.GeometryCollection geometryCollection) {
    List<mil.nga.sf.Geometry> geometries = geometryCollection.getGeometries();
    return geometryFactory.createGeometryCollection(
        geometries.stream().map(this::asJtsGeometry).toArray(Geometry[]::new));
  }

  /**
   * Converts a GeoPackage multi polygon to a JTS multipolygon.
   *
   * @param multiPolygon the GeoPackage multipolygon
   * @return the JTS multipolygon
   */
  private MultiPolygon asJtsMultiPolygon(mil.nga.sf.MultiPolygon multiPolygon) {
    return geometryFactory.createMultiPolygon(
        multiPolygon.getPolygons().stream().map(this::asJtsPolygon).toArray(Polygon[]::new));
  }

  /**
   * Converts a GeoPackage multilinestring to a JTS multi line string.
   *
   * @param multiLineString the GeoPackage multi line string
   * @return the JTS multi line string
   */
  private MultiLineString asJtsMultiLineString(mil.nga.sf.MultiLineString multiLineString) {
    return geometryFactory.createMultiLineString(multiLineString.getLineStrings().stream()
        .map(this::asJtsLineString).toArray(LineString[]::new));
  }

  /**
   * Converts a GeoPackage multipoint to a JTS multipoint.
   * 
   * @param multiPoint the GeoPackage multipoint
   * @return the JTS multipoint
   */
  private MultiPoint asJtsMultiPoint(mil.nga.sf.MultiPoint multiPoint) {
    return geometryFactory.createMultiPoint(
        multiPoint.getPoints().stream().map(this::asJtsPoint).toArray(Point[]::new));
  }

  /**
   * Converts a GeoPackage polygon to a JTS polygon.
   * 
   * @param polygon the GeoPackage polygon
   * @return the JTS polygon
   */
  private Polygon asJtsPolygon(mil.nga.sf.Polygon polygon) {
    var shell = geometryFactory.createLinearRing(polygon.getExteriorRing().getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new));
    var holes = polygon.getRings().stream().skip(1)
        .map(lineString -> geometryFactory.createLinearRing(lineString.getPoints().stream()
            .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new)))
        .toArray(LinearRing[]::new);
    return geometryFactory.createPolygon(shell, holes);
  }

  /**
   * Converts a GeoPackage linestring to a JTS linestring.
   *
   * @param lineString the GeoPackage linestring
   * @return the JTS linestring
   */
  private LineString asJtsLineString(mil.nga.sf.LineString lineString) {
    var coordinates = lineString.getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new);
    return geometryFactory.createLineString(coordinates);
  }

  /**
   * Converts a GeoPackage point to a JTS point.
   *
   * @param point the GeoPackage point
   * @return the JTS point
   */
  private Point asJtsPoint(mil.nga.sf.Point point) {
    return geometryFactory.createPoint(new Coordinate(point.getX(), point.getY()));
  }

  /**
   * An iterator over the rows of a GeoPackage table.
   */
  public class GeopackageIterator implements Iterator<Row> {

    private final FeatureResultSet featureResultSet;

    private final Schema schema;

    private boolean hasNext;

    /**
     * Constructs an iterator from a feature result set.
     *
     * @param featureResultSet the feature result set
     * @param schema the schema of the table
     */
    public GeopackageIterator(FeatureResultSet featureResultSet, Schema schema) {
      this.featureResultSet = featureResultSet;
      this.schema = schema;
      this.hasNext = featureResultSet.moveToFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row next() {
      if (!hasNext) {
        throw new NoSuchElementException();
      }
      Row row = schema.createRow();
      for (FeatureColumn featureColumn : featureResultSet.getColumns().getColumns()) {
        var value = featureResultSet.getValue(featureColumn);
        if (value != null) {
          row.set(featureColumn.getName(), asJavaValue(value));
        }
      }
      hasNext = featureResultSet.moveToNext();
      return row;
    }
  }

}
