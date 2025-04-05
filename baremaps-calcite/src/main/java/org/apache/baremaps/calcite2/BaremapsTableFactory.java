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

package org.apache.baremaps.calcite2;

import org.apache.baremaps.calcite2.data.DataModifiableTable;
import org.apache.baremaps.calcite2.data.DataRow;
import org.apache.baremaps.calcite2.data.DataRowType;
import org.apache.baremaps.calcite2.data.DataSchema;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TableFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A table factory for creating tables in the calcite2 package.
 */
public class BaremapsTableFactory implements TableFactory<Table> {

  /**
   * Constructor.
   */
  public BaremapsTableFactory() {
  }

  @Override
  public Table create(
      SchemaPlus schema,
      String name,
      Map<String, Object> operand,
      RelDataType rowType) {
    final RelProtoDataType protoRowType =
        rowType != null ? RelDataTypeImpl.proto(rowType) : null;
    String format = (String) operand.get("format");
    
    // Create a type factory - Calcite doesn't expose one through SchemaPlus
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    
    return switch (format) {
      case "data" -> createDataTable(name, operand, protoRowType, typeFactory);
      default -> throw new RuntimeException("Unsupported format: " + format);
    };
  }

  /**
   * Creates a Baremaps table.
   *
   * @param name the table name
   * @param operand the operand properties
   * @param protoRowType the prototype row type
   * @param typeFactory the type factory to use
   * @return the created table
   */
  private Table createDataTable(
      String name,
      Map<String, Object> operand,
      RelProtoDataType protoRowType,
      RelDataTypeFactory typeFactory) {
    String directory = (String) operand.get("directory");
    if (directory == null) {
      throw new RuntimeException("A directory should be specified");
    }
    try {
      Memory<MappedByteBuffer> memory = new MemoryMappedDirectory(Paths.get(directory));
      ByteBuffer header = memory.header();
      long size = header.getLong();
      int length = header.getInt();
      byte[] bytes = new byte[length];
      header.get(bytes);
      DataSchema dataSchema = DataSchema.read(new ByteArrayInputStream(bytes), typeFactory);
      DataRowType dataRowType = new DataRowType(dataSchema);
      DataCollection<DataRow> dataCollection = AppendOnlyLog.<DataRow>builder()
          .dataType(dataRowType)
          .memory(memory)
          .build();
      return new DataModifiableTable(
          name, 
          dataSchema, 
          dataCollection, 
          typeFactory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
} 