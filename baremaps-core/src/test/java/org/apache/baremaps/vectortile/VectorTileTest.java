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

package org.apache.baremaps.vectortile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class VectorTileTest {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  @Test
  public void endToEnd() {
    var tile = new VectorTile(List.of(
        new Layer("layer", 256, List.of(
            new Feature(1l, Map.of("a", 1.0, "b", "2"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(1, 2))),
            new Feature(2l, Map.of("c", 3.0, "d", "4"),
                GEOMETRY_FACTORY.createPoint(new Coordinate(2, 3)))))));

    var encoded = new VectorTileEncoder().encodeTile(tile);
    var decoded = new VectorTileDecoder().decodeTile(encoded);

    assertEquals(tile, decoded);
  }

  @Test
  public void file() throws IOException {
    var path = Path.of("src/test/resources/vectortile/14-8493-5795.mvt");
    try (var input = new GZIPInputStream(Files.newInputStream(path))) {
      var buffer = ByteBuffer.wrap(input.readAllBytes());
      var parsed = org.apache.baremaps.mvt.binary.VectorTile.Tile.parseFrom(buffer);
      var tile = new VectorTileDecoder().decodeTile(parsed);
      assertNotNull(tile);
      assertEquals(13, tile.getLayers().size());
    }
  }
}
