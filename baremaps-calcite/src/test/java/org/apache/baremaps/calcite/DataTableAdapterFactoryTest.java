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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.baremaps.BaremapsDataTable;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.data.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class DataTableAdapterFactoryTest {

  @Test
  public void createCsvTable() throws Exception {
    File file = File.createTempFile("test", ".csv");
    String csv = """
        ID,NAME,GEOM
        1,Paris,POINT(2.3522 48.8566)
        2,New York,POINT(-74.0060 40.7128)
        """;
    try (PrintWriter writer = new PrintWriter(file)) {
      writer.write(csv);
    }
    String model = """
        {
            version: '1.0',
            defaultSchema: 'TEST',
            schemas: [
                {
                name: 'TEST',
                tables: [
                    {
                      name: 'TEST',
                      factory: 'org.apache.baremaps.calcite.DataTableFactory',
                      operand: {
                          format: 'csv',
                          file: '%s'
                      }
                    }
                ]
              }
            ]
        }
        """.formatted(file.getAbsolutePath());
    try (Connection connection =
        DriverManager.getConnection("jdbc:calcite:model=inline:" + model)) {

      ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM TEST.TEST");
      while (resultSet.next()) {
        System.out.println(resultSet.getString("ID") + " " + resultSet.getString("GEOM"));
      }
    } finally {
      file.delete();
    }
  }

  @Test
  public void createMMapTable() throws Exception {
    Path path = Files.createTempDirectory("temp");

    DataSchema dataSchema = new DataSchema("test", List.of(
        new DataColumnFixed("id", Cardinality.REQUIRED, Type.INTEGER),
        new DataColumnFixed("name", Cardinality.REQUIRED, Type.STRING),
        new DataColumnFixed("geom", Cardinality.REQUIRED, Type.GEOMETRY)));

    // Serialize the schema
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataSchema.write(output, dataSchema);
    byte[] bytes = output.toByteArray();

    // Write the schema to the header of the memory mapped file
    Memory<MappedByteBuffer> memory = new MemoryMappedDirectory(path);
    ByteBuffer header = memory.header();
    header.position(Long.BYTES);
    header.putInt(bytes.length);
    header.put(bytes);

    DataTable dataTable =
        new BaremapsDataTable(dataSchema,
            new AppendOnlyLog<>(new DataRowType(dataSchema), memory));
    dataTable.add(new DataRow(dataSchema,
        List.of(1, "a", new GeometryFactory().createPoint(new Coordinate(1, 1)))));
    dataTable.add(new DataRow(dataSchema,
        List.of(2, "b", new GeometryFactory().createPoint(new Coordinate(2, 2)))));

    dataTable.close();


    String model = """
        {
            version: '1.0',
            defaultSchema: 'TEST',
            schemas: [
                {
                name: 'TEST',
                tables: [
                    {
                      name: 'TEST',
                      factory: 'org.apache.baremaps.calcite.DataTableFactory',
                      operand: {
                          format: 'baremaps',
                          directory: '%s'
                      }
                    }
                ]
              }
            ]
        }
        """.formatted(path.toAbsolutePath());
    try (Connection connection =
        DriverManager.getConnection("jdbc:calcite:model=inline:" + model)) {

      ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM TEST.TEST");
      while (resultSet.next()) {
        System.out.println(resultSet.getString("ID") + " " + resultSet.getString("GEOM"));
      }
    } finally {
      FileUtils.deleteRecursively(path);
    }
  }
}
