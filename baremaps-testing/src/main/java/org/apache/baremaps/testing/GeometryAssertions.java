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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

public class GeometryAssertions {

  private GeometryAssertions() {
    // Prevent instantiation
  }

  private static void throwAssertionError(Object expected, Object actual) {
    throw new AssertionError("Expected " + expected + " but was " + actual);
  }

  public static void assertGeometryEquals(String wkt, Geometry actual) {
    try {
      assertGeometryEquals(new WKTReader().read(wkt), actual);
    } catch (Exception e) {
      throwAssertionError(wkt, actual);
    }
  }

  public static void assertGeometryEquals(Geometry expected, Geometry actual) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected == null || actual == null) {
      throwAssertionError(expected, actual);
    }
    if (!expected.equals(actual)) {
      throwAssertionError(expected, actual);
    }
  }

  public static void assertGeometryEquals(Geometry expected, Geometry actual, double tolerance) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected == null || actual == null) {
      throwAssertionError(expected, actual);
    }
    PrecisionModel precisionModel = new PrecisionModel(tolerance);
    GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(precisionModel);
    reducer.setPointwise(true);
    assertGeometryEquals(reducer.reduce(expected), reducer.reduce(actual));
  }
}
