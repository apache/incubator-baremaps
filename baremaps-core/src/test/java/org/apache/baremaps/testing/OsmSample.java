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

package org.apache.baremaps.testing;

import java.nio.file.Path;
import org.locationtech.jts.geom.*;

public class OsmSample {

  /* The paths of the sample directory and files */

  public static final Path SAMPLE_DIR = TestFiles.resolve("osm-sample/");

  public static final Path SAMPLE_STATE_TXT = TestFiles.resolve("osm-sample/state.txt");

  public static final Path SAMPLE_OSM_XML = TestFiles.resolve("osm-sample/sample.osm.xml");

  public static final Path SAMPLE_OSM_PBF = TestFiles.resolve("osm-sample/sample.osm.pbf");

  public static final Path SAMPLE_OSC_XML_2 = TestFiles.resolve("osm-sample/000/000/002.osc.gz");

  public static final Path SAMPLE_OSC_XML_3 = TestFiles.resolve("osm-sample/000/000/003.osc.gz");

  public static final Path SAMPLE_OSC_XML_4 = TestFiles.resolve("osm-sample/000/000/004.osc.gz");

  /* The geometries of the osm-sample/sample.osm.xml file */

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  // Node (point)
  public static final Point NODE_POINT_1 = GEOMETRY_FACTORY.createPoint(new Coordinate(0.0, 0.0));

  // Way (line)
  public static final LineString WAY_LINESTRING_4 =
      GEOMETRY_FACTORY.createLineString(new Coordinate[] {
          new Coordinate(1.0, 1.0),
          new Coordinate(2.0, 2.0)
      });

  // Way (polygon)
  public static final Polygon WAY_POLYGON_9 = GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
      new Coordinate(2.0, 2.0),
      new Coordinate(2.0, 3.0),
      new Coordinate(3.0, 3.0),
      new Coordinate(3.0, 2.0),
      new Coordinate(2.0, 2.0)
  });

  // Relation (polygon with hole)
  public static final MultiPolygon RELATION_MULTIPOLYGON_20 = GEOMETRY_FACTORY.createMultiPolygon(
      new Polygon[] {
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(3.0, 3.0),
                  new Coordinate(3.0, 4.0),
                  new Coordinate(4.0, 4.0),
                  new Coordinate(4.0, 3.0),
                  new Coordinate(3.0, 3.0)
              }),
              new LinearRing[] {
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(3.4, 3.4),
                      new Coordinate(3.6, 3.4),
                      new Coordinate(3.6, 3.6),
                      new Coordinate(3.4, 3.6),
                      new Coordinate(3.4, 3.4)
                  })
              })
      });

  // Relation (polygon with island and hole)
  public static final MultiPolygon RELATION_MULTIPOLYGON_36 =
      GEOMETRY_FACTORY.createMultiPolygon(new Polygon[] {
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(4.0, 4.0),
                  new Coordinate(4.0, 4.6),
                  new Coordinate(4.6, 4.6),
                  new Coordinate(4.6, 4.0),
                  new Coordinate(4.0, 4.0)
              }),
              new LinearRing[] {
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(4.2, 4.2),
                      new Coordinate(4.4, 4.2),
                      new Coordinate(4.4, 4.4),
                      new Coordinate(4.2, 4.4),
                      new Coordinate(4.2, 4.2)
                  })
              }),
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(4.8, 4.8),
                  new Coordinate(4.8, 5.0),
                  new Coordinate(5.0, 5.0),
                  new Coordinate(5.0, 4.8),
                  new Coordinate(4.8, 4.8)
              }))
      });

  /* The geometries of the osm-sample/000/000/002.osc.gz file */

  // Node (point)
  public static final Point NODE_POINT_37 = GEOMETRY_FACTORY.createPoint(new Coordinate(6.0, 6.0));

  // Way (line)
  public static final LineString WAY_LINESTRING_40 =
      GEOMETRY_FACTORY.createLineString(new Coordinate[] {
          new Coordinate(7.0, 7.0),
          new Coordinate(8.0, 8.0)
      });

  // Way (polygon)
  public static final Polygon WAY_POLYGON_45 = GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
      new Coordinate(8.0, 8.0),
      new Coordinate(8.0, 9.0),
      new Coordinate(9.0, 9.0),
      new Coordinate(9.0, 8.0),
      new Coordinate(8.0, 8.0)
  });

  // Relation (polygon with hole)
  public static final MultiPolygon RELATION_MULTIPOLYGON_56 = GEOMETRY_FACTORY.createMultiPolygon(
      new Polygon[] {
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(9.0, 9.0),
                  new Coordinate(9.0, 10.0),
                  new Coordinate(10.0, 10.0),
                  new Coordinate(10.0, 9.0),
                  new Coordinate(9.0, 9.0)
              }),
              new LinearRing[] {
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(9.4, 9.4),
                      new Coordinate(9.6, 9.4),
                      new Coordinate(9.6, 9.6),
                      new Coordinate(9.4, 9.6),
                      new Coordinate(9.4, 9.4)
                  })
              })
      });

  // Relation (polygon with island and hole)
  public static final MultiPolygon RELATION_MULTIPOLYGON_72 =
      GEOMETRY_FACTORY.createMultiPolygon(new Polygon[] {
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(10.0, 10.0),
                  new Coordinate(10.0, 10.6),
                  new Coordinate(10.6, 10.6),
                  new Coordinate(10.6, 10.0),
                  new Coordinate(10.0, 10.0)
              }),
              new LinearRing[] {
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(10.2, 10.2),
                      new Coordinate(10.4, 10.2),
                      new Coordinate(10.4, 10.4),
                      new Coordinate(10.2, 10.4),
                      new Coordinate(10.2, 10.2)
                  })
              }),
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(10.8, 10.8),
                  new Coordinate(10.8, 11.0),
                  new Coordinate(11.0, 11.0),
                  new Coordinate(11.0, 10.8),
                  new Coordinate(10.8, 10.8)
              }))
      });

  /* The geometries of the osm-sample/000/000/003.osc.gz file */

  // Node (point)
  public static final Point NODE_POINT_1_MODIFIED =
      GEOMETRY_FACTORY.createPoint(new Coordinate(0.5, 0.5));

  // Way (line)
  public static final LineString WAY_LINESTRING_4_MODIFIED =
      GEOMETRY_FACTORY.createLineString(new Coordinate[] {
          new Coordinate(1.2, 1.2),
          new Coordinate(1.8, 1.8)
      });

  // Way (polygon)
  public static final Polygon WAY_POLYGON_9_MODIFIED =
      GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
          new Coordinate(2.1, 2.1),
          new Coordinate(2.1, 2.9),
          new Coordinate(2.9, 2.9),
          new Coordinate(2.9, 2.1),
          new Coordinate(2.1, 2.1)
      });

  // Relation (polygon with hole)
  public static final MultiPolygon RELATION_MULTIPOLYGON_20_MODIFIED =
      GEOMETRY_FACTORY.createMultiPolygon(
          new Polygon[] {
              GEOMETRY_FACTORY.createPolygon(
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(3.0, 3.0),
                      new Coordinate(3.0, 4.0),
                      new Coordinate(4.0, 4.0),
                      new Coordinate(4.0, 3.0),
                      new Coordinate(3.0, 3.0)
                  }),
                  new LinearRing[] {
                      GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                          new Coordinate(3.5, 3.5),
                          new Coordinate(3.6, 3.5),
                          new Coordinate(3.6, 3.6),
                          new Coordinate(3.5, 3.6),
                          new Coordinate(3.5, 3.5)
                      })
                  })
          });

  public static final MultiPolygon RELATION_MULTIPOLYGON_36_MODIFIED =
      GEOMETRY_FACTORY.createMultiPolygon(new Polygon[] {
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(4.0, 4.0),
                  new Coordinate(4.0, 4.6),
                  new Coordinate(4.6, 4.6),
                  new Coordinate(4.6, 4.0),
                  new Coordinate(4.0, 4.0)
              }),
              new LinearRing[] {
                  GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                      new Coordinate(4.2, 4.2),
                      new Coordinate(4.4, 4.2),
                      new Coordinate(4.4, 4.4),
                      new Coordinate(4.2, 4.4),
                      new Coordinate(4.2, 4.2)
                  })
              }),
          GEOMETRY_FACTORY.createPolygon(
              GEOMETRY_FACTORY.createLinearRing(new Coordinate[] {
                  new Coordinate(4.9, 4.9),
                  new Coordinate(4.9, 5.0),
                  new Coordinate(5.0, 5.0),
                  new Coordinate(5.0, 4.9),
                  new Coordinate(4.9, 4.9)
              }))
      });
}
