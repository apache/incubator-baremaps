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

package org.apache.baremaps.openstreetmap.xml;



import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.OsmReader;
import org.apache.baremaps.openstreetmap.function.CoordinateMapBuilder;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityProjectionTransformer;
import org.apache.baremaps.openstreetmap.function.ReferenceMapBuilder;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.locationtech.jts.geom.Coordinate;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

/** A utility class for parsing an OpenStreetMap XML file. */
public class XmlEntityReader implements OsmReader<Entity> {

  private boolean geometry = false;

  private int srid = 4326;

  private DataMap<Long, Coordinate> coordinateMap;

  private DataMap<Long, List<Long>> referenceMap;

  public boolean geometries() {
    return geometry;
  }

  public XmlEntityReader geometries(boolean geometries) {
    this.geometry = geometries;
    return this;
  }

  public int projection() {
    return srid;
  }

  public XmlEntityReader projection(int srid) {
    this.srid = srid;
    return this;
  }

  public DataMap<Long, Coordinate> coordinateMap() {
    return coordinateMap;
  }

  public XmlEntityReader coordinateMap(DataMap<Long, Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
    return this;
  }

  public DataMap<Long, List<Long>> referenceMap() {
    return referenceMap;
  }

  public XmlEntityReader referenceMap(DataMap<Long, List<Long>> referenceMap) {
    this.referenceMap = referenceMap;
    return this;
  }

  /**
   * Creates an ordered stream of OSM entities from a XML file.
   *
   * @param input
   * @return
   */
  public Stream<Entity> stream(InputStream input) {
    var entities = StreamSupport.stream(new XmlEntitySpliterator(input), false);
    if (geometry) {
      // Initialize and chain the entity handlers
      var coordinateMapBuilder = new CoordinateMapBuilder(coordinateMap);
      var referenceMapBuilder = new ReferenceMapBuilder(referenceMap);
      var entityGeometryBuilder = new EntityGeometryBuilder(coordinateMap, referenceMap);
      var entityProjectionTransformer = new EntityProjectionTransformer(4326, srid);
      var entityHandler = coordinateMapBuilder
              .andThen(referenceMapBuilder)
              .andThen(entityGeometryBuilder)
              .andThen(entityProjectionTransformer);
      entities = entities.map(consumeThenReturn(entityHandler));
    }
    return entities;
  }
}
