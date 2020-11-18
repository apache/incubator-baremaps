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

package com.baremaps.importer.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.MockCache;
import com.baremaps.osm.domain.Info;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Member.MemberType;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.Proj4jException;
import org.locationtech.proj4j.ProjCoordinate;

public class GeometryHandlerTest {

  public static final CRSFactory CRS_FACTORY = new CRSFactory();

  public static final CoordinateReferenceSystem EPSG_4326 = CRS_FACTORY.createFromName("EPSG:4326");

  public static final CoordinateTransform COORDINATE_TRANSFORM = new CoordinateTransform() {
    @Override
    public CoordinateReferenceSystem getSourceCRS() {
      return EPSG_4326;
    }

    @Override
    public CoordinateReferenceSystem getTargetCRS() {
      return EPSG_4326;
    }

    @Override
    public ProjCoordinate transform(ProjCoordinate src, ProjCoordinate tgt) throws Proj4jException {
      return src;
    }
  };

  public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

  public static final LocalDateTime TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

  public static final Info INFO = new Info(0, TIMESTAMP, 0, 0);

  public static final Node NODE_0 = new Node(0, INFO, ImmutableMap.of(), 0, 0);

  public static final Node NODE_1 = new Node(1, INFO, ImmutableMap.of(), 0, 3);

  public static final Node NODE_2 = new Node(2, INFO, ImmutableMap.of(), 3, 3);

  public static final Node NODE_3 = new Node(3, INFO, ImmutableMap.of(), 3, 0);

  public static final Node NODE_4 = new Node(4, INFO, ImmutableMap.of(), 1, 1);

  public static final Node NODE_5 = new Node(5, INFO, ImmutableMap.of(), 1, 2);

  public static final Node NODE_6 = new Node(6, INFO, ImmutableMap.of(), 2, 2);

  public static final Node NODE_7 = new Node(7, INFO, ImmutableMap.of(), 2, 1);

  public static final Node NODE_8 = new Node(8, INFO, ImmutableMap.of(), 4, 1);

  public static final Node NODE_9 = new Node(9, INFO, ImmutableMap.of(), 4, 1);

  public static final Node NODE_10 = new Node(10, INFO, ImmutableMap.of(), 5, 2);

  public static final Node NODE_11 = new Node(11, INFO, ImmutableMap.of(), 5, 1);

  public static final Cache<Long, Coordinate> COORDINATE_CACHE = new MockCache(
      Arrays.asList(NODE_0, NODE_1, NODE_2, NODE_3, NODE_4, NODE_5,
          NODE_6, NODE_7, NODE_8, NODE_9, NODE_10, NODE_11).stream()
          .map(n -> new Coordinate(n.getLon(), n.getLat()))
          .collect(Collectors.toList()));

  public static final Way WAY_0 = new Way(
      0, INFO, ImmutableMap.of(),
      ImmutableList.of());

  public static final Way WAY_1 = new Way(
      1, INFO, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l));

  public static final Way WAY_2 = new Way(
      2, INFO, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l, 0l));

  public static final Way WAY_3 = new Way(
      3, INFO, ImmutableMap.of(),
      ImmutableList.of(8l, 9l, 10l, 11l, 8l));

  public static final Way WAY_4 = new Way(
      4, INFO, ImmutableMap.of(),
      ImmutableList.of(4l, 5l, 6l, 7l, 4l));

  public static final Cache<Long, List<Long>> REFERENCE_CACHE = new MockCache(
      Arrays.asList(WAY_0, WAY_1, WAY_2, WAY_3, WAY_4).stream()
          .map(w -> w.getNodes()).collect(Collectors.toList()));

  public static final Relation RELATION_0 = new Relation(
      0, INFO, ImmutableMap.of(), Arrays.asList());

  public static final Relation RELATION_1 = new Relation(
      1, INFO, ImmutableMap.of("type", "multipolygon"),
      Arrays.asList());

  public static final Relation RELATION_2 = new Relation(
      2, INFO, ImmutableMap.of("type", "multipolygon"),
      Arrays.asList(new Member(2l, MemberType.way, "outer")));

  public static final Relation RELATION_3 = new Relation(
      3, INFO, ImmutableMap.of("type", "multipolygon"),
      Arrays.asList(
          new Member(2l, MemberType.way, "outer"),
          new Member(3l, MemberType.way, "inner")));

  public static final Relation RELATION_4 = new Relation(
      4, INFO, ImmutableMap.of("type", "multipolygon"),
      Arrays.asList(
          new Member(2l, MemberType.way, "outer"),
          new Member(3l, MemberType.way, "inner"),
          new Member(4l, MemberType.way, "outer")));

  public static final GeometryHandler GEOMETRY_BUILDER = new GeometryHandler(COORDINATE_CACHE, REFERENCE_CACHE);

  @Test
  public void handleNode() {
    GEOMETRY_BUILDER.handle(NODE_0);
    Point p0 = (Point) NODE_0.getGeometry();
    assertEquals(p0.getX(), 0);
    assertEquals(p0.getY(), 0);
    GEOMETRY_BUILDER.handle(NODE_2);
    Point p1 = (Point) NODE_2.getGeometry();
    assertEquals(p1.getX(), 3);
    assertEquals(p1.getY(), 3);
  }

  @Test
  public void handleWay() {
    GEOMETRY_BUILDER.handle(WAY_0);
    assertNull(WAY_0.getGeometry());
    GEOMETRY_BUILDER.handle(WAY_1);
    assertTrue(WAY_1.getGeometry() instanceof LineString);
    GEOMETRY_BUILDER.handle(WAY_2);
    assertTrue(WAY_2.getGeometry() instanceof Polygon);
  }

  @Test
  public void handleRelation() {
    GEOMETRY_BUILDER.handle(RELATION_0);
    assertNull(RELATION_0.getGeometry());
    GEOMETRY_BUILDER.handle(RELATION_1);
    assertNull(RELATION_1.getGeometry());
    GEOMETRY_BUILDER.handle(RELATION_2);
    assertTrue(RELATION_2.getGeometry() instanceof Polygon);
    GEOMETRY_BUILDER.handle(RELATION_3);
    assertTrue(RELATION_3.getGeometry() instanceof MultiPolygon);
    GEOMETRY_BUILDER.handle(RELATION_4);
    assertTrue(RELATION_4.getGeometry() instanceof MultiPolygon);
  }


}
