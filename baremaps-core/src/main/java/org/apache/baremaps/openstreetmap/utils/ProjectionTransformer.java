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

package org.apache.baremaps.openstreetmap.utils;



import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A transformer that reprojects geometries and sets the correct output SRIDs. */
public class ProjectionTransformer extends GeometryTransformer {

  private static final Logger logger = LoggerFactory.getLogger(ProjectionTransformer.class);

  private final int sourceSrid;

  private final int targetSrid;

  private final CoordinateTransform transform;

  private ProjCoordinate min;

  private ProjCoordinate max;

  /**
   * Creates a transformer that reprojects geometries with the provided SRIDs.
   *
   * @param sourceSrid the source SRID
   * @param targetSrid the target SRID
   */
  public ProjectionTransformer(int sourceSrid, int targetSrid) {
    this.sourceSrid = sourceSrid;
    this.targetSrid = targetSrid;
    this.transform = GeometryUtils.coordinateTransform(sourceSrid, targetSrid);

    var targetCRS = new CRSFactory().createFromName(String.format("EPSG:%s", targetSrid));
    var lonlatTranform = GeometryUtils.coordinateTransform(4326, sourceSrid);
    min = lonlatTranform
        .transform(new ProjCoordinate(Math.toDegrees(targetCRS.getProjection().getMinLongitude()),
            Math.toDegrees(targetCRS.getProjection().getMinLatitude())), new ProjCoordinate());
    max = lonlatTranform
        .transform(new ProjCoordinate(Math.toDegrees(targetCRS.getProjection().getMaxLongitude()),
            Math.toDegrees(targetCRS.getProjection().getMaxLatitude())), new ProjCoordinate());
  }

  @Override
  protected CoordinateSequence transformCoordinates(CoordinateSequence coordinateSequence,
      Geometry parent) {
    Coordinate[] coordinateArray = Stream.of(coordinateSequence.toCoordinateArray())
        .map(this::transformCoordinate).toArray(Coordinate[]::new);
    return new CoordinateArraySequence(coordinateArray);
  }

  private Coordinate transformCoordinate(Coordinate coordinate) {
    if (sourceSrid == targetSrid) {
      return coordinate;
    }
    var x = Math.max(Math.min(coordinate.x, max.x), min.x);
    var y = Math.max(Math.min(coordinate.y, max.y), min.y);
    ProjCoordinate c1 = new ProjCoordinate(x, y);
    ProjCoordinate c2 = transform.transform(c1, new ProjCoordinate());
    return new Coordinate(c2.x, c2.y);
  }

  protected Geometry transformPoint(Point geom, Geometry parent) {
    try {
      var geometry = super.transformPoint(geom, parent);
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("Point cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformMultiPoint(MultiPoint geom, Geometry parent) {
    try {
      var geometry = super.transformMultiPoint(geom, parent);
      if (geometry instanceof Point point) {
        geometry = factory.createMultiPoint(new Point[] {point});
      }
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("MultiPoint cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
    try {
      var geometry = super.transformLinearRing(geom, parent);
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("LinearRing cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformLineString(LineString geom, Geometry parent) {
    try {
      var geometry = super.transformLineString(geom, parent);
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("LineString cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformMultiLineString(MultiLineString geom, Geometry parent) {
    try {
      var geometry = super.transformMultiLineString(geom, parent);
      if (geometry instanceof LineString lineString) {
        geometry = factory.createMultiLineString(new LineString[] {lineString});
      }
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("MultiLineString cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformPolygon(Polygon geom, Geometry parent) {
    try {
      var geometry = super.transformPolygon(geom, parent);
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("Polygon cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
    try {
      var geometry = super.transformMultiPolygon(geom, parent);
      if (geometry instanceof Polygon polygon) {
        geometry = factory.createMultiPolygon(new Polygon[] {polygon});
      }
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("MultiPolygon cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  protected Geometry transformGeometryCollection(GeometryCollection geom, Geometry parent) {
    try {
      var geometry = super.transformGeometryCollection(geom, parent);
      return withTargetSRID(geometry);
    } catch (Exception e) {
      logger.error("GeometryCollection cannot be reprojected", e);
      return parent.getFactory().createEmpty(0);
    }
  }

  private Geometry withTargetSRID(Geometry outputGeom) {
    outputGeom.setSRID(targetSrid);
    return outputGeom;
  }
}
