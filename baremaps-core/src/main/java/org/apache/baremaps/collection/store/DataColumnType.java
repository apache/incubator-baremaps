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

package org.apache.baremaps.collection.store;

import org.locationtech.jts.geom.*;

public enum DataColumnType {
  BOOLEAN(Boolean.class),
  BYTE(Byte.class),
  SHORT(Short.class),
  INTEGER(Integer.class),
  LONG(Long.class),
  FLOAT(Float.class),
  DOUBLE(Double.class),
  STRING(String.class),
  BYTES(byte[].class),
  GEOMETRY(Geometry.class),
  POINT(Point.class),
  LINESTRING(LineString.class),
  POLYGON(Polygon.class),
  MULTIPOINT(MultiPoint.class),
  MULTILINESTRING(MultiLineString.class),
  MULTIPOLYGON(MultiPolygon.class),
  GEOMETRYCOLLECTION(GeometryCollection.class);

  private Class<?> type;

  DataColumnType(Class<?> type) {
    this.type = type;
  }

  public Class<?> getType() {
    return type;
  }

  public DataColumnType valueOf(Class<?> type) {
    for (DataColumnType columnType : DataColumnType.values()) {
      if (columnType.getType().equals(type)) {
        return columnType;
      }
    }
    throw new IllegalArgumentException("Unsupported type " + type);
  }

}
