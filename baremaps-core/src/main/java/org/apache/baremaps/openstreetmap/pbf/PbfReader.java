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

package org.apache.baremaps.openstreetmap.pbf;



import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.OsmReader;
import org.locationtech.jts.geom.Coordinate;

public interface PbfReader<T> extends OsmReader<T> {

  /**
   * Gets the number of blobs buffered by the parser to parallelize deserialization.
   *
   * @return the size of the buffer
   */
  int buffer();

  /**
   * Sets the number of blobs buffered by the parser to parallelize deserialization.
   *
   * @param buffer the size of the buffer
   * @return the reader
   */
  PbfReader<T> buffer(int buffer);

  /**
   * Gets the flag enabling the generation of geometries.
   *
   * @return the value of the flag
   */
  boolean geometries();

  /**
   * Sets the flag enabling the generation of geometries.
   *
   * @param geometries the value of the flag
   * @return the reader
   */
  PbfReader<T> geometries(boolean geometries);

  /**
   * Gets the projection of the geometries generated by this parser.
   *
   * @return the projection of the geometries
   */
  int projection();

  /**
   * Sets the projection of the geometries generated by this parser.
   *
   * @param srid the projection of the geometries
   * @return the reader
   */
  PbfReader<T> projection(int srid);

  /**
   * Gets the map used to store coordinates for generating geometries.
   *
   * @return the map of coordinates
   */
  DataMap<Long, Coordinate> coordinateMap();

  /**
   * Sets the map used to store coordinates for generating geometries.
   *
   * @param coordinateMap the map of coordinates
   * @return the reader
   */
  PbfReader<T> coordinateMap(DataMap<Long, Coordinate> coordinateMap);

  /**
   * Gets the map used to store references for generating geometries.
   *
   * @return the map of references
   */
  DataMap<Long, List<Long>> referenceMap();

  /**
   * Sets the map used to store references for generating geometries.
   *
   * @param referenceMap the map of references
   * @return the reader
   */
  PbfReader<T> referenceMap(DataMap<Long, List<Long>> referenceMap);

  /**
   * Creates an ordered stream of osm objects.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of osm objects
   */
  Stream<T> stream(InputStream inputStream);
}
