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

package com.baremaps.osm.store;

import com.baremaps.osm.model.Member;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class DatabaseConstants {

  public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps";

  public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

  public static final LocalDateTime TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

  public static final NodeEntity NODE_0 = new NodeEntity(0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 0)));

  public static final NodeEntity NODE_1 = new NodeEntity(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 3)));

  public static final NodeEntity NODE_2 = new NodeEntity(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)));

  public static final NodeEntity NODE_3 = new NodeEntity(
      3, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 0)));

  public static final NodeEntity NODE_4 = new NodeEntity(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)));

  public static final NodeEntity NODE_5 = new NodeEntity(
      5, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 2)));

  public static final NodeEntity NODE_6 = new NodeEntity(
      6, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)));

  public static final NodeEntity NODE_7 = new NodeEntity(
      7, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 1)));

  public static final NodeEntity NODE_8 = new NodeEntity(
      8, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 1)));

  public static final NodeEntity NODE_9 = new NodeEntity(
      9, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 2)));

  public static final NodeEntity NODE_10 = new NodeEntity(
      10, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 2)));

  public static final NodeEntity NODE_11 = new NodeEntity(
      11, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 1)));

  public static final WayEntity WAY_0 = new WayEntity(0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(), null);

  public static final WayEntity WAY_1 = new WayEntity(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l), null);

  public static final WayEntity WAY_2 = new WayEntity(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l, 0l), null);

  public static final WayEntity WAY_3 = new WayEntity(
      3, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(8l, 9l, 10l, 11l, 8l), null);

  public static final WayEntity WAY_4 = new WayEntity(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(4l, 5l, 6l, 7l, 4l), null);

  public static final RelationEntity RELATION_0 = new RelationEntity(
      0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      new Long[0], new String[0], new String[0], null);

  public static final RelationEntity RELATION_1 = new RelationEntity(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[0], new String[0], new String[0], null);

  public static final RelationEntity RELATION_2 = new RelationEntity(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l},
      new String[]{Member.Type.way.name()},
      new String[]{"outer"},
      null);

  public static final RelationEntity RELATION_3 = new RelationEntity(
      3, 0, TIMESTAMP, 0, 0,
      ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l, 3l},
      new String[]{Member.Type.way.name(), Member.Type.way.name()},
      new String[]{"outer", "inner"},
      null);

  public static final RelationEntity RELATION_4 = new RelationEntity(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l, 3l, 4l},
      new String[]{Member.Type.way.name(), Member.Type.way.name(), Member.Type.way.name()},
      new String[]{"outer", "inner", "outer"},
      null);

}
