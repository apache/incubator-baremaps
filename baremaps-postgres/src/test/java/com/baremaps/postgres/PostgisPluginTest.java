/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.postgres.jdbi.Feature;
import com.baremaps.postgres.jdbi.PostgisPlugin;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class PostgisPluginTest {

  @Test
  @Tag("integration")
  void postgisPlugin() {
    PostgisRecord record = new PostgisRecord(1,
        new GeometryFactory().createPoint(new Coordinate(1, 1)),
        new GeometryFactory()
            .createLineString(
                new Coordinate[]{
                    new Coordinate(1, 1),
                    new Coordinate(1, 2),
                    new Coordinate(2, 2),
                    new Coordinate(2, 1)
                }),
        new GeometryFactory()
            .createPolygon(
                new Coordinate[]{
                    new Coordinate(1, 1),
                    new Coordinate(1, 2),
                    new Coordinate(2, 2),
                    new Coordinate(2, 1),
                    new Coordinate(1, 1),
                }));

    Jdbi jdbi = Jdbi.create("jdbc:tc:postgis:13-3.1:///test").installPlugin(new PostgisPlugin());
    List<PostgisRecord> result =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  "CREATE TABLE record (id integer, point geometry(point), linestring geometry(linestring), polygon geometry(polygon))");
              handle
                  .createUpdate(
                      "INSERT INTO record (id, point, linestring, polygon) VALUES (:id, :point, :lineString, :polygon)")
                  .bindBean(record)
                  .execute();
              return handle
                  .createQuery("SELECT * FROM record ORDER BY id")
                  .mapToBean(PostgisRecord.class)
                  .list();
            });

    assertEquals(record.getPoint(), result.get(0).getPoint());
    assertEquals(record.getLineString(), result.get(0).getLineString());
    assertEquals(record.getPolygon(), result.get(0).getPolygon());
  }

  @Test
  @Tag("integration")
  void beanMapper() {
    var id = "1";
    var type = "Feature";
    var geometry = new GeometryFactory().createPoint(new Coordinate(1, 1));
    var properties = Map.of("k1", "v1", "k2", "v2");
    var feature = new Feature(id, type, geometry, properties);

    Jdbi jdbi = Jdbi.create("jdbc:tc:postgis:13-3.1:///test").installPlugin(new PostgisPlugin());
    List<Feature> features =
        jdbi.withHandle(
            handle -> {
              var createExtension = "CREATE EXTENSION IF NOT EXISTS hstore;";
              handle.execute(createExtension);

              var createTable = "CREATE TABLE feature (id text, type text, geometry geometry, properties hstore)";
              handle.execute(createTable);

              var insert = "INSERT INTO feature (id, type, geometry, properties) VALUES (:id, :type, :geometry, :properties)";
              handle.createUpdate(insert)
                  .bindBean(feature)
                  .execute();

              var select = "SELECT * FROM feature ORDER BY id";
              return handle
                  .createQuery(select)
                  .mapToBean(Feature.class)
                  .list();
            });

    assertEquals(feature.getId(), features.get(0).getId());
    assertEquals(feature.getType(), features.get(0).getType());
    assertEquals(feature.getGeometry(), features.get(0).getGeometry());
    assertEquals(feature.getProperties(), features.get(0).getProperties());
  }

  @Test
  @Tag("integration")
  void rowMapper() {
    var id = "id";
    var gid = "gid";
    var type = "type";
    var title = "title";
    var description = "description";
    var area = 1.5d;
    var point = new GeometryFactory().createPoint(new Coordinate(1, 1));
    var geom = new GeometryFactory().createPoint(new Coordinate(2, 2));

    Jdbi jdbi = Jdbi.create("jdbc:tc:postgis:13-3.1:///test").installPlugin(new PostgisPlugin());
    List<Feature> features =
        jdbi.withHandle(
            handle -> {
              var createExtension = "CREATE EXTENSION IF NOT EXISTS hstore;";
              handle.execute(createExtension);

              var createTable = "CREATE TABLE feature (id text, gid text, type text, point geometry(point), geom geometry, title text, description text, area float)";
              handle.execute(createTable);

              var insert = "INSERT INTO feature (id, gid, type, point, geom, title, description, area) VALUES (:id, :gid,  :type, :point, :geom, :title, :description, :area)";
              handle.createUpdate(insert)
                  .bind("id", id)
                  .bind("gid", gid)
                  .bind("type", type)
                  .bind("point", point)
                  .bind("geom", geom)
                  .bind("title", title)
                  .bind("description", description)
                  .bind("area", area)
                  .execute();

              var select = "SELECT * FROM feature ORDER BY id";
              return handle
                  .createQuery(select)
                  .mapTo(Feature.class) // uses the FeatureMapper registered by the plugin
                  .list();
            });

    assertEquals(id, features.get(0).getId());
    assertEquals(type, features.get(0).getType());
    assertEquals(point, features.get(0).getGeometry());
    assertEquals(gid, features.get(0).getProperties().get("gid"));
    assertEquals(title, features.get(0).getProperties().get("title"));
    assertEquals(String.valueOf(area), features.get(0).getProperties().get("area"));
  }
}
