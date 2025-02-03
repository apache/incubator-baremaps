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

package org.apache.baremaps.geocoder;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.lucene.document.*;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRowMapper implements Function<DataRow, Document> {

  private static final Logger logger = LoggerFactory.getLogger(DataRowMapper.class);

  @Override
  public Document apply(DataRow dataRow) {
    Document doc = new Document();
    DataSchema schema = dataRow.schema();
    List<DataColumn> columns = schema.columns();
    for (int i = 0; i < columns.size(); i++) {
      Object value = dataRow.get(i);
      if (value == null) {
        continue;
      }

      DataColumn column = columns.get(i);
      applyValue(column, doc, value);
    }
    return doc;
  }

  @SuppressWarnings("squid:S6541")
  private void applyValue(DataColumn column, Document doc, Object value) {
    String columnName = column.name();
    Type type = column.type();
    try {
      switch (type) {
        case BINARY:
          doc.add(new StoredField(columnName, (byte[]) value));
          break;
        case BYTE:
          doc.add(new IntPoint(columnName, ((Byte) value).intValue()));
          doc.add(new StoredField(columnName, ((Byte) value).intValue()));
          break;
        case BOOLEAN:
          doc.add(new StringField(columnName, value.toString(), Field.Store.YES));
          break;
        case SHORT:
          doc.add(new IntPoint(columnName, ((Short) value).intValue()));
          doc.add(new StoredField(columnName, ((Short) value).intValue()));
          break;
        case INTEGER:
          doc.add(new IntPoint(columnName, (Integer) value));
          doc.add(new StoredField(columnName, (Integer) value));
          break;
        case LONG:
          doc.add(new LongPoint(columnName, (Long) value));
          doc.add(new StoredField(columnName, (Long) value));
          break;
        case FLOAT:
          doc.add(new FloatPoint(columnName, (Float) value));
          doc.add(new StoredField(columnName, (Float) value));
          break;
        case DOUBLE:
          doc.add(new DoublePoint(columnName, (Double) value));
          doc.add(new StoredField(columnName, (Double) value));
          break;
        case STRING:
          doc.add(new TextField(columnName, (String) value, Field.Store.YES));
          break;
        case COORDINATE:
          Coordinate coord = (Coordinate) value;
          double lat = coord.getY();
          double lon = coord.getX();
          doc.add(new LatLonPoint(columnName, lat, lon));
          doc.add(new StoredField(columnName + "_lat", lat));
          doc.add(new StoredField(columnName + "_lon", lon));
          break;
        case POINT:
          Point point = (Point) value;
          double pointLat = point.getY();
          double pointLon = point.getX();
          doc.add(new LatLonPoint(columnName, pointLat, pointLon));
          doc.add(new StoredField(columnName + "_lat", pointLat));
          doc.add(new StoredField(columnName + "_lon", pointLon));
          break;
        case LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, GEOMETRYCOLLECTION, GEOMETRY:
          Geometry geometry = (Geometry) value;
          if (geometry != null) {
            Field[] shapeFields = createShapeFields(columnName, geometry);
            for (Field field : shapeFields) {
              doc.add(field);
            }
            doc.add(new StoredField(columnName + "_wkt", geometry.toText()));
          }
          break;
        case ENVELOPE:
          Envelope envelope = (Envelope) value;
          String envelopeStr = envelope.toString();
          doc.add(new StringField(columnName, envelopeStr, Field.Store.YES));
          break;
        case INET_ADDRESS, INET4_ADDRESS, INET6_ADDRESS:
          InetAddress addr = (InetAddress) value;
          doc.add(new StringField(columnName, addr.getHostAddress(), Field.Store.YES));
          break;
        case LOCAL_DATE:
          LocalDate date = (LocalDate) value;
          doc.add(new StringField(columnName, date.toString(), Field.Store.YES));
          break;
        case LOCAL_TIME:
          LocalTime time = (LocalTime) value;
          doc.add(new StringField(columnName, time.toString(), Field.Store.YES));
          break;
        case LOCAL_DATE_TIME:
          LocalDateTime dateTime = (LocalDateTime) value;
          doc.add(new StringField(columnName, dateTime.toString(), Field.Store.YES));
          break;
        case NESTED:
          Map<String, Object> map = (Map<String, Object>) value;
          for (Map.Entry<String, Object> entry : map.entrySet()) {
            String nestedKey = columnName + "." + entry.getKey();
            Object nestedValue = entry.getValue();
            if (nestedValue != null) {
              doc.add(new TextField(nestedKey, nestedValue.toString(), Field.Store.YES));
            }
          }
          break;
        default:
          doc.add(new StringField(columnName, value.toString(), Field.Store.YES));
          break;
      }
    } catch (Exception e) {
      logger.error("Error processing column '{}' with value '{}': {}", columnName, value,
          e.getMessage());
    }
  }

  private Field[] createShapeFields(String fieldName, Geometry geometry) {
    if (geometry instanceof Point point) {
      double lat = point.getY();
      double lon = point.getX();
      return new Field[] {new LatLonPoint(fieldName, lat, lon)};
    } else if (geometry instanceof LineString lineString) {
      return LatLonShape.createIndexableFields(fieldName, convertToLuceneLine(lineString));
    } else if (geometry instanceof Polygon polygon) {
      org.apache.lucene.geo.Polygon lucenePolygon = convertToLucenePolygon(polygon);
      return LatLonShape.createIndexableFields(fieldName, lucenePolygon);
    } else if (geometry instanceof MultiPolygon multiPolygon) {
      return createFieldsFromMultiPolygon(fieldName, multiPolygon);
    } else if (geometry instanceof GeometryCollection collection) {
      List<Field> fieldList = new ArrayList<>();
      for (int i = 0; i < collection.getNumGeometries(); i++) {
        Geometry geom = collection.getGeometryN(i);
        Field[] fields = createShapeFields(fieldName, geom);
        fieldList.addAll(Arrays.asList(fields));
      }
      return fieldList.toArray(new Field[0]);
    } else {
      logger.warn("Unsupported geometry type '{}' for field '{}'", geometry.getGeometryType(),
          fieldName);
      return new Field[0];
    }
  }

  private org.apache.lucene.geo.Line convertToLuceneLine(LineString lineString) {
    Coordinate[] coords = lineString.getCoordinates();
    double[] lats = new double[coords.length];
    double[] lons = new double[coords.length];
    for (int i = 0; i < coords.length; i++) {
      lats[i] = coords[i].getY();
      lons[i] = coords[i].getX();
    }
    return new org.apache.lucene.geo.Line(lats, lons);
  }

  private org.apache.lucene.geo.Polygon convertToLucenePolygon(
      org.locationtech.jts.geom.Polygon jtsPolygon) {
    LinearRing shell = jtsPolygon.getExteriorRing();
    Coordinate[] shellCoords = shell.getCoordinates();
    double[] lats = new double[shellCoords.length];
    double[] lons = new double[shellCoords.length];
    for (int i = 0; i < shellCoords.length; i++) {
      lats[i] = shellCoords[i].getY();
      lons[i] = shellCoords[i].getX();
    }

    int numHoles = jtsPolygon.getNumInteriorRing();
    org.apache.lucene.geo.Polygon[] holes = new org.apache.lucene.geo.Polygon[numHoles];
    for (int i = 0; i < numHoles; i++) {
      LinearRing hole = jtsPolygon.getInteriorRingN(i);
      Coordinate[] holeCoords = hole.getCoordinates();
      double[] holeLats = new double[holeCoords.length];
      double[] holeLons = new double[holeCoords.length];
      for (int j = 0; j < holeCoords.length; j++) {
        holeLats[j] = holeCoords[j].getY();
        holeLons[j] = holeCoords[j].getX();
      }
      holes[i] = new org.apache.lucene.geo.Polygon(holeLats, holeLons);
    }

    return new org.apache.lucene.geo.Polygon(lats, lons, holes);
  }

  private Field[] createFieldsFromMultiPolygon(String fieldName, MultiPolygon multiPolygon) {
    List<Field> fieldList = new ArrayList<>();
    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
      org.locationtech.jts.geom.Polygon polygon =
          (org.locationtech.jts.geom.Polygon) multiPolygon.getGeometryN(i);
      org.apache.lucene.geo.Polygon lucenePolygon = convertToLucenePolygon(polygon);
      Field[] fields = LatLonShape.createIndexableFields(fieldName, lucenePolygon);
      fieldList.addAll(Arrays.asList(fields));
    }
    return fieldList.toArray(new Field[0]);
  }
}
