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

package org.apache.baremaps.calcite;

import java.util.function.UnaryOperator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A decorator for a {@link DataTable} that applies a geometry transformation to each row.
 */
public class DataTableGeometryMapper implements UnaryOperator<DataRow> {

  private final DataTable table;

  private final GeometryTransformer mapper;

  /**
   * Constructs a new data table transformer with the specified data table and geometry transformer.
   *
   * @param table the data table to transform
   * @param mapper the geometry mapper
   */
  public DataTableGeometryMapper(DataTable table, GeometryTransformer mapper) {
    this.table = table;
    this.mapper = mapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRow apply(DataRow row) {
    var columns = table.schema()
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
