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

package org.apache.baremaps.utils;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A transformer that rounds the coordinates of a geometry to a given precision.
 */
public class RoundingTransformer extends GeometryTransformer {

  private int precision;

  /**
   * Constructs a transformer that rounds the coordinates of a geometry to a given precision.
   *
   * @param precision the precision
   */
  public RoundingTransformer(int precision) {
    this.precision = precision;
  }

  /**
   * Rounds the coordinates of a geometry to a given precision.
   *
   * @param sequence the coordinate sequence
   * @param parent the parent geometry
   * @return the geometry
   */
  @Override
  protected CoordinateSequence transformCoordinates(CoordinateSequence sequence, Geometry parent) {
    CoordinateSequence rounded = super.transformCoordinates(sequence, parent);
    for (int i = 0; i < rounded.size(); i++) {
      double roundedX =
          Math.round(rounded.getOrdinate(i, CoordinateSequence.X) * Math.pow(10, precision))
              / Math.pow(10, precision);
      double roundedY =
          Math.round(rounded.getOrdinate(i, CoordinateSequence.Y) * Math.pow(10, precision))
              / Math.pow(10, precision);
      rounded.setOrdinate(i, CoordinateSequence.X, roundedX);
      rounded.setOrdinate(i, CoordinateSequence.Y, roundedY);
    }
    return rounded;
  }
}
