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

package org.apache.baremaps.data.schema;

import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A decorator for a {@link DataFrame} that applies a geometry transformation to each row.
 */
public class DataFrameGeometryMapper implements Function<DataRow, DataRow> {

  private final DataFrame frame;

  private final GeometryTransformer mapper;

  /**
   * Constructs a new data frame transformer with the specified data frame and geometry transformer.
   *
   * @param frame the data frame to transform
   * @param mapper the geometry mapper
   */
  public DataFrameGeometryMapper(DataFrame frame, GeometryTransformer mapper) {
    this.frame = frame;
    this.mapper = mapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRow apply(DataRow row) {
    var columns = frame.schema()
        .columns().stream()
        .filter(column -> column.type().binding().isAssignableFrom(Geometry.class))
        .toList();
    for (DataColumn column : columns) {
      var name = column.name();
      var geometry = (Geometry) row.get(name);
      if (geometry != null) {
        row.set(name, mapper.transform(geometry));
      }
    }
    return row;
  }
}
