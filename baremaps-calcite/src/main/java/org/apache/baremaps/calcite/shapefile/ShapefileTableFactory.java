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

package org.apache.baremaps.calcite.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TableFactory;

/**
 * A table factory for creating Shapefile tables.
 */
public class ShapefileTableFactory implements TableFactory<Table> {

  /**
   * Constructor.
   */
  public ShapefileTableFactory() {}

  @Override
  public Table create(
      SchemaPlus schema,
      String name,
      Map<String, Object> operand,
      RelDataType rowType) {
    // Get the file path from the operand
    String filePath = (String) operand.get("file");
    if (filePath == null) {
      throw new IllegalArgumentException("File path must be specified in the 'file' operand");
    }

    try {
      File file = new File(filePath);
      return new ShapefileTable(file);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create ShapefileTable from file: " + filePath, e);
    }
  }
}
