package com.baremaps.storage.geopackage;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import mil.nga.crs.common.DateTime;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.GeometryType;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
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
import org.opengis.parameter.ParameterValueGroup;

public class GeoPackageTableStore extends DataStore implements FeatureSet {

  private final FeatureDao featureDao;

  private final FeatureType featureType;

  private final GeometryFactory geometryFactory;

  public GeoPackageTableStore(FeatureDao featureDao) {
    this.featureDao = featureDao;
    FeatureTypeBuilder builder = new FeatureTypeBuilder().setName(featureDao.getTableName());
    for (FeatureColumn column : featureDao.getColumns()) {
      if (column.isGeometry()) {
        builder.addAttribute(jtsType(column.getGeometryType()))
            .setName(column.getName())
            .setMinimumOccurs(column.isNotNull() ? 1 : 0);
      } else {
        builder.addAttribute(sisType(column.getDataType()))
            .setName(column.getName())
            .setMinimumOccurs(column.isNotNull() ? 1 : 0);
      }
    }
    featureType = builder.build();
    geometryFactory = new GeometryFactory(new PrecisionModel(), (int) featureDao.getSrs().getId());
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ParameterValueGroup> getOpenParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws DataStoreException {

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
        if (featureColumn.isGeometry()) {
          GeoPackageGeometryData value = (GeoPackageGeometryData) featureResultSet.getValue(featureColumn);
          feature.setPropertyValue(featureColumn.getName(), jtsValue(value.getGeometry()));
        } else {
          Object value = featureResultSet.getValue(featureColumn);
          feature.setPropertyValue(featureColumn.getName(), sisValue(value));
        }
      }
      hasNext = featureResultSet.moveToNext();
      return feature;
    }
  }

  private Class<?> sisType(GeoPackageDataType dataType) {
    return dataType.getClassType();
  }

  private Object sisValue(Object value) {
    if (value instanceof Date || value instanceof DateTime) {
      return value.toString();
    } else {
      return value;
    }
  }

  private Class<?> jtsType(GeometryType type) {
    return switch (type) {
      case POINT -> Point.class;
      case LINESTRING -> LineString.class;
      case POLYGON -> Polygon.class;
      case MULTIPOINT -> MultiPoint.class;
      case MULTILINESTRING -> MultiLineString.class;
      case MULTIPOLYGON -> MultiPolygon.class;
      case GEOMETRYCOLLECTION -> GeometryCollection.class;
      case GEOMETRY, CIRCULARSTRING, COMPOUNDCURVE, CURVEPOLYGON, MULTICURVE, MULTISURFACE, CURVE, SURFACE, POLYHEDRALSURFACE, TIN, TRIANGLE -> Geometry.class;
    };
  }

  private Geometry jtsValue(mil.nga.sf.Geometry geometry) {
    if (geometry instanceof mil.nga.sf.Point point) {
      return jtsPoint(point);
    } else if (geometry instanceof mil.nga.sf.LineString lineString) {
      return jtsLineString(lineString);
    } else if (geometry instanceof mil.nga.sf.Polygon polygon) {
      return jtsPolygon(polygon);
    } else if (geometry instanceof mil.nga.sf.MultiPoint multiPoint) {
      return jtsMultiPoint(multiPoint);
    } else if (geometry instanceof mil.nga.sf.MultiLineString multiLineString) {
      return jtsMultiLineString(multiLineString);
    } else if (geometry instanceof mil.nga.sf.MultiPolygon multiPolygon) {
      return jtsMultiPolygon(multiPolygon);
    } else if (geometry instanceof mil.nga.sf.GeometryCollection geometryCollection) {
      return jstGeometryCollection(geometryCollection);
    } else {
      // Unknown geometries are discarded
      return geometryFactory.createEmpty(0);
    }
  }

  private GeometryCollection jstGeometryCollection(mil.nga.sf.GeometryCollection geometryCollection) {
    List<mil.nga.sf.Geometry> geometries = geometryCollection.getGeometries();
    return geometryFactory.createGeometryCollection(geometries.stream()
        .map(this::jtsValue)
        .toArray(Geometry[]::new));
  }

  private MultiPolygon jtsMultiPolygon(mil.nga.sf.MultiPolygon multiPolygon) {
    return geometryFactory.createMultiPolygon(multiPolygon.getPolygons().stream()
        .map(this::jtsPolygon)
        .toArray(Polygon[]::new));
  }

  private MultiLineString jtsMultiLineString(mil.nga.sf.MultiLineString multiLineString) {
    return geometryFactory.createMultiLineString(multiLineString.getLineStrings().stream()
        .map(this::jtsLineString)
        .toArray(LineString[]::new));
  }

  private MultiPoint jtsMultiPoint(mil.nga.sf.MultiPoint multiPoint) {
    return geometryFactory.createMultiPoint(multiPoint.getPoints().stream()
        .map(this::jtsPoint)
        .toArray(Point[]::new));
  }

  private Polygon jtsPolygon(mil.nga.sf.Polygon polygon) {
    var shell = geometryFactory.createLinearRing(polygon.getExteriorRing().getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY()))
        .toArray(Coordinate[]::new));
    var holes = polygon.getRings().stream().skip(1)
        .map(lineString -> geometryFactory.createLinearRing(lineString.getPoints().stream()
            .map(point -> new Coordinate(point.getX(), point.getY()))
            .toArray(Coordinate[]::new)))
        .toArray(LinearRing[]::new);
    return geometryFactory.createPolygon(shell, holes);
  }

  private LineString jtsLineString(mil.nga.sf.LineString lineString) {
    var coordinates = lineString.getPoints().stream()
        .map(point -> new Coordinate(point.getX(), point.getY()))
        .toArray(Coordinate[]::new);
    return geometryFactory.createLineString(coordinates);
  }

  private Point jtsPoint(mil.nga.sf.Point point) {
    return geometryFactory.createPoint(new Coordinate(point.getX(), point.getY()));
  }

}
