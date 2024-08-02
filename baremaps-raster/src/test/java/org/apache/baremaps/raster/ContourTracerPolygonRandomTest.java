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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Random;
import org.junit.jupiter.api.Test;

class ContourTracerPolygonRandomTest {

  @Test
  void randomTest() {
    assertDoesNotThrow(() -> {
      var size = 256;
      var random = new Random(0);
      double[] grid = new double[size * size];
      for (int j = 0; j < grid.length; j++) {
        grid[j] = random.nextDouble();
      }
      var polygons = new ContourTracer(grid, size, size, false, true).traceContours(0.5);
    });
  }
}
