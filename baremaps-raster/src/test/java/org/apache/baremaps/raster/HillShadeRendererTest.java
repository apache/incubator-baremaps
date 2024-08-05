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

import org.junit.jupiter.api.Test;

class HillShadeRendererTest {

  @Test
  void testCalculate() {
    double[] dem = new double[] {
        2450, 2461, 2483,
        2452, 2461, 2483,
        2447, 2455, 2477,
    };
    HillshadeCalculator hillshadeCalculator = new HillshadeCalculator(dem, 3, 3, 5);
    double[] hillshade = hillshadeCalculator.calculate(315, 45);
    assertEquals(154.0286599079096, hillshade[4]);
  }



}
