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

package org.apache.baremaps.database.schema;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.locationtech.jts.geom.*;

/**
 * A column in a table.
 */
public interface DataColumn {

  /**
   * Returns the name of the column.
   *
   * @return the name of the column
   */
  String name();

  /**
   * Returns the type of the column.
   *
   * @return the type of the column
   */
  Type type();

  /**
   * An enumeration of the supported data column types.
   */
  enum Type {
    BYTE(Byte.class),
    BYTE_ARRAY(byte[].class),
    BOOLEAN(Boolean.class),
    BOOLEAN_ARRAY(boolean[].class),
    SHORT(Short.class),
    SHORT_ARRAY(short[].class),
    INTEGER(Integer.class),
    INTEGER_ARRAY(int[].class),
    LONG(Long.class),
    LONG_ARRAY(long[].class),
    FLOAT(Float.class),
    FLOAT_ARRAY(float[].class),
    DOUBLE(Double.class),
    DOUBLE_ARRAY(double[].class),
    STRING(String.class),
    COORDINATE(Coordinate.class),
    GEOMETRY(Geometry.class),
    POINT(Point.class),
    LINESTRING(LineString.class),
    POLYGON(Polygon.class),
    MULTIPOINT(MultiPoint.class),
    MULTILINESTRING(MultiLineString.class),
    MULTIPOLYGON(MultiPolygon.class),
    GEOMETRYCOLLECTION(GeometryCollection.class),
    INET_ADDRESS(InetAddress.class),
    INET4_ADDRESS(Inet4Address.class),
    INET6_ADDRESS(Inet6Address.class),
    LOCAL_DATE(LocalDate.class),
    LOCAL_TIME(LocalTime.class),
    LOCAL_DATE_TIME(LocalDateTime.class),;

    private final Class<?> binding;

    Type(Class<?> binding) {
      this.binding = binding;
    }

    public Class<?> binding() {
      return binding;
    }

    public static Type fromBinding(Class<?> binding) {
      for (Type type : Type.values()) {
        if (type.binding().equals(binding)) {
          return type;
        }
      }
      throw new IllegalArgumentException("Unsupported binding: " + binding);
    }
  }
}
