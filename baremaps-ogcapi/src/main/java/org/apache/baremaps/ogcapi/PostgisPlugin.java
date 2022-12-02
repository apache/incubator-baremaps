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

package org.apache.baremaps.ogcapi;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.StatementContext;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class PostgisPlugin extends JdbiPlugin.Singleton {

  @Override
  public void customizeJdbi(Jdbi jdbi) {
    // Register argument factories
    jdbi.registerArgument(new PointArgumentFactory());
    jdbi.registerArgument(new LineStringArgumentFactory());
    jdbi.registerArgument(new LinearRingArgumentFactory());
    jdbi.registerArgument(new PolygonArgumentFactory());
    jdbi.registerArgument(new MultiPointArgumentFactory());
    jdbi.registerArgument(new MultiLineStringArgumentFactory());
    jdbi.registerArgument(new MultiPolygonArgumentFactory());
    jdbi.registerArgument(new GeometryCollectionArgumentFactory());
    jdbi.registerArgument(new GeometryArgumentFactory());

    // Register column mappers
    jdbi.registerColumnMapper(new PointColumnMapper());
    jdbi.registerColumnMapper(new LineStringColumnMapper());
    jdbi.registerColumnMapper(new LinearRingColumnMapper());
    jdbi.registerColumnMapper(new PolygonColumnMapper());
    jdbi.registerColumnMapper(new MultiPointColumnMapper());
    jdbi.registerColumnMapper(new MultiLineStringColumnMapper());
    jdbi.registerColumnMapper(new MultiPolygonColumnMapper());
    jdbi.registerColumnMapper(new GeometryCollectionColumnMapper());
    jdbi.registerColumnMapper(new GeometryColumnMapper());
  }

  abstract static class BaseArgumentFactory<T extends Geometry> extends AbstractArgumentFactory<T> {

    public BaseArgumentFactory() {
      super(Types.OTHER);
    }

    @Override
    public Argument build(T value, ConfigRegistry config) {
      return (position, statement, ctx) -> statement.setBytes(position,
          GeometryUtils.serialize(value));
    }
  }

  abstract static class BaseColumnMapper<T extends Geometry> implements ColumnMapper<T> {

    @Override
    public T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
      byte[] bytes = hexStringToByteArray(r.getString(columnNumber));
      return (T) GeometryUtils.deserialize(bytes);
    }

    private static byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
        data[i / 2] =
            (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }
      return data;
    }
  }

  static final class GeometryArgumentFactory extends BaseArgumentFactory<Geometry> {
  }

  static final class GeometryCollectionArgumentFactory
      extends BaseArgumentFactory<GeometryCollection> {
  }

  static final class GeometryCollectionColumnMapper extends BaseColumnMapper<GeometryCollection> {
  }

  static final class GeometryColumnMapper extends BaseColumnMapper<Geometry> {
  }

  static final class LinearRingArgumentFactory extends BaseArgumentFactory<LinearRing> {
  }

  static final class LinearRingColumnMapper extends BaseColumnMapper<LinearRing> {
  }

  static final class LineStringArgumentFactory extends BaseArgumentFactory<LineString> {
  }

  static final class LineStringColumnMapper extends BaseColumnMapper<LineString> {
  }

  static final class MultiLineStringArgumentFactory extends BaseArgumentFactory<MultiLineString> {
  }

  static final class MultiLineStringColumnMapper extends BaseColumnMapper<MultiLineString> {
  }

  static final class MultiPointArgumentFactory extends BaseArgumentFactory<MultiPoint> {
  }

  static final class MultiPointColumnMapper extends BaseColumnMapper<MultiPoint> {
  }

  static final class MultiPolygonArgumentFactory extends BaseArgumentFactory<MultiPolygon> {
  }

  static final class MultiPolygonColumnMapper extends BaseColumnMapper<MultiPolygon> {
  }

  static final class PointArgumentFactory extends BaseArgumentFactory<Point> {
  }

  static final class PointColumnMapper extends BaseColumnMapper<Point> {
  }

  static final class PolygonArgumentFactory extends BaseArgumentFactory<Polygon> {
  }

  static final class PolygonColumnMapper extends BaseColumnMapper<Polygon> {
  }
}
