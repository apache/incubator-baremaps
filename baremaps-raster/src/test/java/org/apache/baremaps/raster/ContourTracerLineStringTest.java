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

package org.apache.baremaps.raster;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class ContourTracerLineStringTest {

  @Test
  @DisplayName("Test case 0")
  void testProcessCellWithCase00() {
    var grid = new double[] {
        0, 0,
        0, 0,
    };
    var lines = trace(grid);
    assertTrue(lines.isEmpty());
  }

  @Test
  @DisplayName("Test case 1")
  void testProcessCellWithCase01() {
    var grid = new double[] {
        1, 0,
        0, 0,
    };
    var lines = trace(grid);
    assertEquals("LINESTRING (0 0.5, 0.5 0)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 2")
  void testProcessCellWithCase02() {
    var grid = new double[] {
        0, 1,
        0, 0,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 0, 1 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 3")
  void testProcessCellWithCase03() {
    var grid = new double[] {
        1, 1,
        0, 0,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0 0.5, 1 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 4")
  void testProcessCellWithCase04() {
    var grid = new double[] {
        0, 0,
        0, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (1 0.5, 0.5 1)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 5")
  void testProcessCellWithCase05() {
    var grid = new double[] {
        1, 0,
        0, 1,
    };
    var lines = trace(grid);
    assertEquals(2, lines.size());
    assertEquals("LINESTRING (0 0.5, 0.5 1)", lines.get(0).toString());
    assertEquals("LINESTRING (1 0.5, 0.5 0)", lines.get(1).toString());
  }

  @Test
  @DisplayName("Test case 6")
  void testProcessCellWithCase06() {
    var grid = new double[] {
        0, 1,
        0, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 0, 0.5 1)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 7")
  void testProcessCellWithCase07() {
    var grid = new double[] {
        1, 1,
        0, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0 0.5, 0.5 1)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 8")
  void testProcessCellWithCase08() {
    var grid = new double[] {
        0, 0,
        1, 0,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 1, 0 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 9")
  void testProcessCellWithCase09() {
    var grid = new double[] {
        1, 0,
        1, 0,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 1, 0.5 0)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 10")
  void testProcessCellWithCase10() {
    var grid = new double[] {
        0, 1,
        1, 0,
    };
    var lines = trace(grid);
    assertEquals(2, lines.size());
    assertEquals("LINESTRING (0.5 0, 0 0.5)", lines.get(0).toString());
    assertEquals("LINESTRING (0.5 1, 1 0.5)", lines.get(1).toString());
  }

  @Test
  @DisplayName("Test case 11")
  void testProcessCellWithCase11() {
    var grid = new double[] {
        1, 1,
        1, 0,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 1, 1 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 12")
  void testProcessCellWithCase12() {
    var grid = new double[] {
        0, 0,
        1, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (1 0.5, 0 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 13")
  void testProcessCellWithCase13() {
    var grid = new double[] {
        1, 0,
        1, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (1 0.5, 0.5 0)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 14")
  void testProcessCellWithCase14() {
    var grid = new double[] {
        0, 1,
        1, 1,
    };
    var lines = trace(grid);
    assertEquals(1, lines.size());
    assertEquals("LINESTRING (0.5 0, 0 0.5)", lines.get(0).toString());
  }

  @Test
  @DisplayName("Test case 15")
  void testProcessCellWithCase15() {
    var grid = new double[] {
        1, 1,
        1, 1,
    };
    var lines = trace(grid);
    assertTrue(lines.isEmpty());
  }

  List<Geometry> trace(double[] grid) {
    return new ContourTracer(grid, 2, 2, false, false).traceContours(0.5);
  }

}
