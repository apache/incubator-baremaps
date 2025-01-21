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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.baremaps.csv.CsvDataTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TableFactory;

public class BaremapsTableFactory implements TableFactory<BaremapsTable> {

  public BaremapsTableFactory() {

  }

  @Override
  public BaremapsTable create(SchemaPlus schema, String name,
      Map<String, Object> operand, RelDataType rowType) {
    final RelProtoDataType protoRowType =
        rowType != null ? RelDataTypeImpl.proto(rowType) : null;
    String file = (String) operand.get("file");
    if (file != null && file.endsWith(".csv")) {
      try {
        return new BaremapsTable(
            new CsvDataTable(new File(file), true),
            protoRowType);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new BaremapsTable(null, protoRowType);
  }
}
