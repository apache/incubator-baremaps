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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.baremaps.calcite.csv.CsvTable;
import org.apache.baremaps.calcite.data.DataModifiableTable;
import org.apache.baremaps.calcite.data.DataRow;
import org.apache.baremaps.calcite.data.DataRowType;
import org.apache.baremaps.calcite.data.DataTableSchema;
import org.apache.baremaps.calcite.flatgeobuf.FlatGeoBufTable;
import org.apache.baremaps.calcite.geopackage.GeoPackageTable;
import org.apache.baremaps.calcite.geoparquet.GeoParquetTable;
import org.apache.baremaps.calcite.openstreetmap.OpenStreetMapTable;
import org.apache.baremaps.calcite.rpsl.RpslTable;
import org.apache.baremaps.calcite.shapefile.ShapefileTable;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.openstreetmap.xml.XmlEntityReader;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TableFactory;

/**
 * A table factory for creating tables in the calcite2 package.
 */
public class BaremapsTableFactory implements TableFactory<Table> {

  /**
   * Constructor.
   */
  public BaremapsTableFactory() {}

  @Override
  public Table create(
      SchemaPlus schema,
      String name,
      Map<String, Object> operand,
      RelDataType rowType) {
    String format = (String) operand.get("format");

    // Create a type factory - Calcite doesn't expose one through SchemaPlus
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

    return switch (format) {
      case "data" -> createDataTable(name, operand, typeFactory);
      case "osm" -> createOpenStreetMapTable(operand);
      case "csv" -> createCsvTable(operand);
      case "shp" -> createShapefileTable(operand);
      case "rpsl" -> createRpslTable(operand);
      case "fgb" -> createFlatGeoBufTable(operand);
      case "parquet" -> createGeoParquetTable(operand);
      case "geopackage" -> createGeoPackageTable(operand);
      default -> throw new RuntimeException("Unsupported format: " + format);
    };
  }

  /**
   * Creates a Baremaps table.
   *
   * @param name the table name
   * @param operand the operand properties
   * @param typeFactory the type factory to use
   * @return the created table
   */
  private Table createDataTable(
      String name,
      Map<String, Object> operand,
      RelDataTypeFactory typeFactory) {
    String directory = (String) operand.get("directory");
    if (directory == null) {
      throw new RuntimeException("A directory should be specified");
    }
    try {
      Memory<MappedByteBuffer> memory = new MemoryMappedDirectory(Paths.get(directory));
      ByteBuffer header = memory.header();
      header.getLong(); // Skip the size
      int length = header.getInt();
      byte[] bytes = new byte[length];
      header.get(bytes);
      DataTableSchema dataTableSchema = DataTableSchema.read(new ByteArrayInputStream(bytes), typeFactory);
      DataRowType dataRowType = new DataRowType(dataTableSchema);
      DataCollection<DataRow> dataCollection = AppendOnlyLog.<DataRow>builder()
          .dataType(dataRowType)
          .memory(memory)
          .build();
      return new DataModifiableTable(
          name,
              dataTableSchema,
          dataCollection,
          typeFactory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an OpenStreetMap table from a file.
   *
   * @param operand the operand properties
   * @return the created table
   */
  private Table createOpenStreetMapTable(Map<String, Object> operand) {
    // Get the file path from the operand
    String filePath = (String) operand.get("file");
    if (filePath == null) {
      throw new IllegalArgumentException("File path must be specified in the 'file' operand");
    }

    try {
      // Create a new input stream from the file
      InputStream inputStream = new FileInputStream(filePath);

      // Create an entity reader based on the file extension
      if (filePath.endsWith(".pbf") || filePath.endsWith(".osm.pbf")) {
        return createTableFromPbf(Paths.get(filePath));
      } else if (filePath.endsWith(".xml") || filePath.endsWith(".osm")) {
        return createTableFromXml(Paths.get(filePath));
      } else {
        throw new IllegalArgumentException(
            "Unsupported file format. Supported formats are .pbf, .osm.pbf, .xml, and .osm");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to create OpenStreetMapTable from file: " + filePath, e);
    }
  }

  /**
   * Creates a CSV table from a file.
   *
   * @param operand the operand properties
   * @return the created table
   */
  private Table createCsvTable(Map<String, Object> operand) {
    // Get the file path from the operand
    String filePath = (String) operand.get("file");
    if (filePath == null) {
      throw new IllegalArgumentException("File path must be specified in the 'file' operand");
    }

    // Get the separator (default to comma)
    String separatorStr = (String) operand.getOrDefault("separator", ",");
    if (separatorStr.length() != 1) {
      throw new IllegalArgumentException("Separator must be a single character");
    }
    char separator = separatorStr.charAt(0);

    // Get whether the file has a header (default to true)
    boolean hasHeader = (Boolean) operand.getOrDefault("hasHeader", true);

    try {
      File file = new File(filePath);
      return new CsvTable(file, separator, hasHeader);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create CsvTable from file: " + filePath, e);
    }
  }

  /**
   * Creates a shapefile table from a file.
   *
   * @param operand the operand properties
   * @return the created table
   */
  private Table createShapefileTable(Map<String, Object> operand) {
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

  /**
   * Creates a RPSL table from a file.
   *
   * @param operand the operand properties
   * @return the created table
   */
  private Table createRpslTable(Map<String, Object> operand) {
    // Get the file path from the operand
    String filePath = (String) operand.get("file");
    if (filePath == null) {
      throw new IllegalArgumentException("File path must be specified in the 'file' operand");
    }

    try {
      File file = new File(filePath);
      return new RpslTable(file);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create RpslTable from file: " + filePath, e);
    }
  }

  /**
   * Creates a FlatGeoBuf table from a file.
   *
   * @param operand the operand properties
   * @return the created table
   */
  private Table createFlatGeoBufTable(Map<String, Object> operand) {
    // Get the file path from the operand
    String filePath = (String) operand.get("file");
    if (filePath == null) {
      throw new IllegalArgumentException("File path must be specified in the 'file' operand");
    }

    try {
      File file = new File(filePath);
      return new FlatGeoBufTable(file);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create FlatGeoBufTable from file: " + filePath, e);
    }
  }

  /**
   * Create a table from a PBF file.
   *
   * @param path the path to the PBF file
   * @return the table
   * @throws IOException if an I/O error occurs
   */
  public static OpenStreetMapTable createTableFromPbf(Path path) throws IOException {
    PbfEntityReader reader = new PbfEntityReader();
    reader.setGeometries(true);
    return new OpenStreetMapTable(path.toFile(), reader);
  }

  /**
   * Create a table from an XML file.
   *
   * @param path the path to the XML file
   * @return the table
   * @throws IOException if an I/O error occurs
   */
  public static OpenStreetMapTable createTableFromXml(Path path) throws IOException {
    XmlEntityReader reader = new XmlEntityReader();
    reader.setGeometries(true);
    return new OpenStreetMapTable(path.toFile(), reader);
  }

  private Table createGeoParquetTable(Map<String, Object> operand) {
    if (operand.size() < 2) {
      throw new IllegalArgumentException("Missing file path for GeoParquet table");
    }
    try {
      String filePath = (String) operand.get("file");
      if (filePath == null) {
        throw new IllegalArgumentException("File path must be specified in the 'file' operand");
      }

      // Create a type factory - Calcite doesn't expose one through SchemaPlus
      RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

      return new GeoParquetTable(new File(filePath), typeFactory);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create GeoParquet table", e);
    }
  }

  private Table createGeoPackageTable(Map<String, Object> operand) {
    if (operand.size() < 2) {
      throw new IllegalArgumentException("Missing file path and table name for GeoPackage table");
    }
    try {
      String filePath = (String) operand.get("file");
      if (filePath == null) {
        throw new IllegalArgumentException("File path must be specified in the 'file' operand");
      }

      String tableName = (String) operand.get("table");
      if (tableName == null) {
        throw new IllegalArgumentException("Table name must be specified in the 'table' operand");
      }

      // Create a type factory - Calcite doesn't expose one through SchemaPlus
      RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

      return new GeoPackageTable(new File(filePath), tableName, typeFactory);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create GeoPackage table", e);
    }
  }
}
