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

package org.apache.baremaps.database.database;



import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Info;
import org.apache.baremaps.openstreetmap.model.Member;
import org.apache.baremaps.openstreetmap.model.Member.MemberType;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

class Constants {

  public static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), 4326);

  public static final LocalDateTime TIMESTAMP = LocalDateTime.of(2020, 1, 1, 0, 0);

  public static final Info INFO = new Info(0, TIMESTAMP, 0, 0);

  public static final Header HEADER_0 = new Header(1l, TIMESTAMP, "", "", "");

  public static final Header HEADER_1 = new Header(2l, TIMESTAMP, "", "", "");

  public static final Header HEADER_2 = new Header(3l, TIMESTAMP, "", "", "");

  public static final Node NODE_0 = new Node(0, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 0)));

  public static final Node NODE_1 = new Node(1, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(0, 3)));

  public static final Node NODE_2 = new Node(2, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)));

  public static final Node NODE_3 = new Node(3, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(3, 0)));

  public static final Node NODE_4 = new Node(4, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)));

  public static final Node NODE_5 = new Node(5, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(1, 2)));

  public static final Node NODE_6 = new Node(6, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)));

  public static final Node NODE_7 = new Node(7, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(2, 1)));

  public static final Node NODE_8 = new Node(8, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 1)));

  public static final Node NODE_9 = new Node(9, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(4, 2)));

  public static final Node NODE_10 = new Node(10, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 2)));

  public static final Node NODE_11 = new Node(11, INFO, ImmutableMap.of(), 0, 0,
      GEOMETRY_FACTORY.createPoint(new Coordinate(5, 1)));

  public static final Way WAY_0 = new Way(0, INFO, ImmutableMap.of(), ImmutableList.of(), null);

  public static final Way WAY_1 =
      new Way(1, INFO, ImmutableMap.of(), ImmutableList.of(0l, 1l, 2l, 3l), null);

  public static final Way WAY_2 =
      new Way(2, INFO, ImmutableMap.of(), ImmutableList.of(0l, 1l, 2l, 3l, 0l), null);

  public static final Way WAY_3 =
      new Way(3, INFO, ImmutableMap.of(), ImmutableList.of(8l, 9l, 10l, 11l, 8l), null);

  public static final Way WAY_4 =
      new Way(4, INFO, ImmutableMap.of(), ImmutableList.of(4l, 5l, 6l, 7l, 4l), null);

  public static final Relation RELATION_0 =
      new Relation(0, INFO, ImmutableMap.of(), ImmutableList.of(), null);

  public static final Relation RELATION_1 =
      new Relation(1, INFO, ImmutableMap.of("type", "multipolygon"), ImmutableList.of(), null);

  public static final Relation RELATION_2 =
      new Relation(2, INFO, ImmutableMap.of("type", "multipolygon"),
          ImmutableList.of(new Member(2, MemberType.WAY, "outer")), null);

  public static final Relation RELATION_3 =
      new Relation(
          3, INFO, ImmutableMap.of("type", "multipolygon"), ImmutableList
              .of(new Member(2, MemberType.WAY, "outer"), new Member(3, MemberType.WAY, "inner")),
          null);

  public static final Relation RELATION_4 =
      new Relation(4, INFO, ImmutableMap.of("type", "multipolygon"),
          ImmutableList.of(new Member(2, MemberType.WAY, "outer"),
              new Member(3, MemberType.WAY, "inner"), new Member(4, MemberType.WAY, "outer")),
          null);
}
