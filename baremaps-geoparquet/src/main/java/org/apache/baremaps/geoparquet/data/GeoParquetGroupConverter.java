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

package org.apache.baremaps.geoparquet.data;

import org.apache.parquet.io.api.Converter;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;

class GeoParquetGroupConverter extends GroupConverter {

  private final GeoParquetGroupConverter parent;
  private final int index;
  protected GeoParquetGroupImpl current;
  private Converter[] converters;

  GeoParquetGroupConverter(GeoParquetGroupConverter parent, int index,
      GroupType schema) {
    this.parent = parent;
    this.index = index;

    converters = new Converter[schema.getFieldCount()];

    for (int i = 0; i < converters.length; i++) {
      final Type type = schema.getType(i);
      if (type.isPrimitive()) {
        converters[i] = new GeoParquetPrimitiveConverter(this, i);
      } else {
        converters[i] = new GeoParquetGroupConverter(this, i, type.asGroupType());
      }

    }
  }

  @Override
  public void start() {
    current = parent.getCurrentRecord().addGroup(index);
  }

  @Override
  public Converter getConverter(int fieldIndex) {
    return converters[fieldIndex];
  }

  @Override
  public void end() {}

  public GeoParquetGroupImpl getCurrentRecord() {
    return current;
  }
}
