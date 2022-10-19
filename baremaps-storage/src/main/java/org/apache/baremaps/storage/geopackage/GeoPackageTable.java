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



import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import org.apache.sis.feature.builder.AttributeRole;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class GeoPackageTable implements FeatureSet {

  private final FeatureDao featureDao;

  private final FeatureType featureType;

  private final GeometryFactory geometryFactory;

  protected GeoPackageTable(FeatureDao featureDao) {
    this.featureDao = featureDao;
    var typeBuilder = new FeatureTypeBuilder().setName(featureDao.getTableName());
    for (FeatureColumn column : featureDao.getColumns()) {
      var attributeBuilder = typeBuilder.addAttribute(classType(column)).setName(column.getName())
          .setMinimumOccurs(column.isNotNull() ? 1 : 0);
      if (column.isPrimaryKey()) {
        attributeBuilder.addRole(AttributeRole.IDENTIFIER_COMPONENT);
      }
    }
    featureType = typeBuilder.build();
    geometryFactory = new GeometryFactory(new PrecisionModel(), (int) featureDao.getSrs().getId());
  }

  private Class<?> classType(FeatureColumn column) {
    if (column.isGeometry()) {
      return Geometry.class;
    } else {
      return column.getDataType().getClassType();
    }
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType,
      StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FeatureType getType() throws DataStoreException {
    return featureType;
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    Iterator<Feature> featureIterator = new FeatureIterator(featureDao.queryForAll(), featureType);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(featureIterator, 0), false);
  }

  public class FeatureIterator implements Iterator<Feature> {

    private final FeatureResultSet featureResultSet;

    private final FeatureType featureType;

    private boolean hasNext;

    public FeatureIterator(FeatureResultSet featureResultSet, FeatureType featureType) {
      this.featureResultSet = featureResultSet;
      this.featureType = featureType;
      this.hasNext = featureResultSet.moveToFirst();
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public Feature next() {
      if (!hasNext) {
        throw new NoSuchElementException();
      }
      Feature feature = featureType.newInstance();
      for (FeatureColumn featureColumn : featureResultSet.getColumns().getColumns()) {
        var value = featureResultSet.getValue(featureColumn);
        if (value != null) {
          feature.setPropertyValue(featureColumn.getName(), asValue(value));
        }
      }
      hasNext = featureResultSet.moveToNext();
      return feature;
    }
  }

  private Object asValue(Object value) {
    if (value instanceof GeoPackageGeometryData geometry) {
      return asJtsGeometry(geometry.getGeometry());
    } else if (value instanceof Date date) {
      return value.toString();
    } else {
      return value;
    }
  }

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

  private GeometryCollection asJstGeometryCollection(
      mil.nga.sf.GeometryCollection geometryCollection) {
    List<mil.nga.sf.Geometry> geometries = geometryCollection.getGeometries();
    return geometryFactory.createGeometryCollection(
        geometries.stream().map(this::asJtsGeometry).toArray(Geometry[]::new));
  }

  private MultiPolygon asJtsMultiPolygon(mil.nga.sf.MultiPolygon multiPolygon) {
    return geometryFactory.createMultiPolygon(
        multiPolygon.getPolygons().stream().map(this::asJtsPolygon).toArray(Polygon[]::new));
  }

  private MultiLineString asJtsMultiLineString(mil.nga.sf.MultiLineString multiLineString) {
    return geometryFactory.createMultiLineString(multiLineString.getLineStrings().stream()
        .map(this::asJtsLineString).toArray(LineString[]::new));
  }

  private MultiPoint asJtsMultiPoint(mil.nga.sf.MultiPoint multiPoint) {
    return geometryFactory.createMultiPoint(
        multiPoint.getPoints().stream().map(this::asJtsPoint).toArray(Point[]::new));
  }

  private Polygon asJtsPolygon(mil.nga.sf.Polygon polygon) {
    var shell = geometryFactory.createLinearRing(polygon.getExteriorRing().getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new));
    var holes = polygon.getRings().stream().skip(1)
        .map(lineString -> geometryFactory.createLinearRing(lineString.getPoints().stream()
            .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new)))
        .toArray(LinearRing[]::new);
    return geometryFactory.createPolygon(shell, holes);
  }

  private LineString asJtsLineString(mil.nga.sf.LineString lineString) {
    var coordinates = lineString.getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY())).toArray(Coordinate[]::new);
    return geometryFactory.createLineString(coordinates);
  }

  private Point asJtsPoint(mil.nga.sf.Point point) {
    return geometryFactory.createPoint(new Coordinate(point.getX(), point.getY()));
  }
}
