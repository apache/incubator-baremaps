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

import org.apache.baremaps.csv.CsvDataTable;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.data.store.DataTableImpl;
import org.apache.baremaps.data.type.DataType;
import org.apache.baremaps.data.type.DataTypeImpl;
import org.apache.baremaps.store.DataRow;
import org.apache.baremaps.store.DataSchema;
import org.apache.baremaps.store.DataTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TableFactory;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class BaremapsTableFactory implements TableFactory<BaremapsTable> {

    public BaremapsTableFactory() {

    }

    @Override
    public BaremapsTable create(
            SchemaPlus schema,
            String name,
            Map<String, Object> operand,
            RelDataType rowType) {
        final RelProtoDataType protoRowType =
                rowType != null ? RelDataTypeImpl.proto(rowType) : null;
        String format = (String) operand.get("format");
        DataTable dataTable = switch (format) {
            case "mmap" -> createMMapTable(schema, name, operand, rowType);
            case "csv" -> createCsvTable(schema, name, operand, rowType);
            default -> throw new RuntimeException("Unsupported format");
        };
        return new BaremapsTable(dataTable, protoRowType);
    }

    private DataTable createMMapTable(SchemaPlus schema, String name, Map<String, Object> operand, RelDataType rowType) {
        String directory = (String) operand.get("directory");
        if (directory == null) {
            throw new RuntimeException("A directory should be specified");
        }
        try {
            Path directoryPath = Paths.get(directory);
            Path schemaFile = directoryPath.resolve("schema.json");
            DataSchema dataSchema = DataSchema.read(schemaFile);
            DataType<DataRow> dataType = new DataTypeImpl(dataSchema);
            Memory<MappedByteBuffer> memory = new MemoryMappedDirectory(directoryPath);
            DataCollection<DataRow> dataCollection = new AppendOnlyLog<>(dataType, memory);
            return new DataTableImpl(dataSchema, dataCollection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataTable createCsvTable(SchemaPlus schema, String name, Map<String, Object> operand, RelDataType rowType) {
        String file = (String) operand.get("file");
        if (file == null) {
            throw new RuntimeException("A file should be specified");
        }
        try {
            return new CsvDataTable(name, new File(file), true, ',');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
