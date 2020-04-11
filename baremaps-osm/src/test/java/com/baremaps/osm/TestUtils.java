/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.osm;

import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Info;
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.osmpbf.FileBlock;
import com.baremaps.osm.osmpbf.FileBlock.Type;
import com.baremaps.osm.store.Store;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.Proj4jException;
import org.locationtech.proj4j.ProjCoordinate;

public class TestUtils {

  private static final CRSFactory CRS_FACTORY = new CRSFactory();
  private static final CoordinateReferenceSystem EPSG_4326 = CRS_FACTORY.createFromName("EPSG:4326");

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

  public static final NodeBuilder NODE_BUILDER = new NodeBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY);

  public static final LocalDateTime TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

  public static final NodeTable.Node NODE_0 = new NodeTable.Node(0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 0)));

  public static final NodeTable.Node NODE_1 = new NodeTable.Node(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 3)));

  public static final NodeTable.Node NODE_2 = new NodeTable.Node(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)));

  public static final NodeTable.Node NODE_3 = new NodeTable.Node(
      3, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 0)));

  public static final NodeTable.Node NODE_4 = new NodeTable.Node(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)));

  public static final NodeTable.Node NODE_5 = new NodeTable.Node(
      5, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 2)));

  public static final NodeTable.Node NODE_6 = new NodeTable.Node(
      6, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)));

  public static final NodeTable.Node NODE_7 = new NodeTable.Node(
      7, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 1)));

  public static final NodeTable.Node NODE_8 = new NodeTable.Node(
      8, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 1)));

  public static final NodeTable.Node NODE_9 = new NodeTable.Node(
      9, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 2)));

  public static final NodeTable.Node NODE_10 = new NodeTable.Node(
      10, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 2)));

  public static final NodeTable.Node NODE_11 = new NodeTable.Node(
      11, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 1)));

  public static final List<NodeTable.Node> NODE_LIST = ImmutableList.of(
      NODE_0, NODE_1, NODE_2, NODE_3, NODE_4, NODE_5,
      NODE_6, NODE_7, NODE_8, NODE_9, NODE_10, NODE_11);

  public static final Store<Long, Coordinate> COORDINATE_STORE = new Store<Long, Coordinate>() {
    @Override
    public Coordinate get(Long key) {
      NodeTable.Node node = NODE_LIST.get(key.intValue());
      return new Coordinate(node.getPoint().getX(), node.getPoint().getY());
    }

    @Override
    public List<Coordinate> getAll(List<Long> keys) {
      List<Coordinate> coordinateList = new ArrayList<>();
      for (Long key : keys) {
        coordinateList.add(get(key));
      }
      return coordinateList;
    }

    @Override
    public void put(Long key, Coordinate values) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(List<Entry<Long, Coordinate>> storeEntries) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Long key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(List<Long> keys) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void importAll(List<Entry<Long, Coordinate>> values) {
      throw new UnsupportedOperationException();
    }

  };

  public static final WayBuilder WAY_BUILDER = new WayBuilder(COORDINATE_TRANSFORM, GEOMETRY_FACTORY,
      COORDINATE_STORE);

  public static final WayTable.Way WAY_0 = new WayTable.Way(0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(), null);

  public static final WayTable.Way WAY_1 = new WayTable.Way(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l), null);

  public static final WayTable.Way WAY_2 = new WayTable.Way(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(0l, 1l, 2l, 3l, 0l), null);

  public static final WayTable.Way WAY_3 = new WayTable.Way(
      3, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(8l, 9l, 10l, 11l, 8l), null);

  public static final WayTable.Way WAY_4 = new WayTable.Way(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      ImmutableList.of(4l, 5l, 6l, 7l, 4l), null);

  public static final List<WayTable.Way> WAY_LIST = ImmutableList.of(WAY_0, WAY_1, WAY_2, WAY_3, WAY_4);

  public static final Store<Long, List<Long>> REFERENCE_STORE = new Store<Long, List<Long>>() {
    @Override
    public List<Long> get(Long key) {
      return WAY_LIST.get(key.intValue()).getNodes();
    }

    @Override
    public List<List<Long>> getAll(List<Long> keys) {
      List<List<Long>> referenceList = new ArrayList<>();
      for (Long key : keys) {
        referenceList.add(get(key));
      }
      return referenceList;
    }

    @Override
    public void put(Long key, List<Long> values) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(List<Entry<Long, List<Long>>> storeEntries) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Long key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(List<Long> keys) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void importAll(List<Entry<Long, List<Long>>> values) {
      throw new UnsupportedOperationException();
    }
  };


  public static final RelationTable.Relation RELATION_0 = new RelationTable.Relation(
      0, 0, TIMESTAMP, 0, 0, ImmutableMap.of(),
      new Long[0], new String[0], new String[0], null);

  public static final RelationTable.Relation RELATION_1 = new RelationTable.Relation(
      1, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[0], new String[0], new String[0], null);

  public static final RelationTable.Relation RELATION_2 = new RelationTable.Relation(
      2, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l},
      new String[]{Member.Type.way.name()},
      new String[]{"outer"},
      null);

  public static final RelationTable.Relation RELATION_3 = new RelationTable.Relation(
      3, 0, TIMESTAMP, 0, 0,
      ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l, 3l},
      new String[]{Member.Type.way.name(), Member.Type.way.name()},
      new String[]{"outer", "inner"},
      null);

  public static final RelationTable.Relation RELATION_4 = new RelationTable.Relation(
      4, 0, TIMESTAMP, 0, 0, ImmutableMap.of("type", "multipolygon"),
      new Long[]{2l, 3l, 4l},
      new String[]{Member.Type.way.name(), Member.Type.way.name(), Member.Type.way.name()},
      new String[]{"outer", "inner", "outer"},
      null);

  public static final List<RelationTable.Relation> RELATION_LIST = ImmutableList
      .of(RELATION_0, RELATION_2, RELATION_3, RELATION_4);

  public static final RelationBuilder RELATION_BUILDER = new RelationBuilder(COORDINATE_TRANSFORM,
      GEOMETRY_FACTORY,
      COORDINATE_STORE,
      REFERENCE_STORE);

  public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/osm?allowMultiQueries=true&user=osm&password=osm";

  public static InputStream dataOsmPbf() {
    return TestUtils.class.getClassLoader().getResourceAsStream("data.osm.pbf");
  }

  public static InputStream denseOsmPbf() {
    return TestUtils.class.getClassLoader().getResourceAsStream("dense.osm.pbf");
  }

  public static InputStream waysOsmPbf() {
    return TestUtils.class.getClassLoader().getResourceAsStream("ways.osm.pbf");
  }

  public static InputStream relationsOsmPbf() {
    return TestUtils.class.getClassLoader().getResourceAsStream("relations.osm.pbf");
  }

  public static FileBlock invalidOsmPbf() {
    return new FileBlock(Type.OSMHeader, ByteString.copyFromUtf8(""), ByteString.copyFromUtf8(""));
  }

  public static InputStream dataOsmXml() {
    return TestUtils.class.getClassLoader().getResourceAsStream("data.osm.xml");
  }

  public static InputStream dataOscXml() {
    return TestUtils.class.getClassLoader().getResourceAsStream("data.osc.xml");
  }

}
