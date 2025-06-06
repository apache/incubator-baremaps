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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * A Calcite schema implementation for shapefile data. This schema provides access to shapefiles
 * through the Apache Calcite framework for SQL querying.
 */
public class ShapefileSchema extends AbstractSchema {

  private final File directory;
  private final Map<String, Table> tableMap;
  private final RelDataTypeFactory typeFactory;

  /**
   * Constructs a ShapefileSchema with the specified directory.
   *
   * @param directory the directory containing shapefiles
   * @param typeFactory the type factory to use for creating tables
   * @throws IOException if an I/O error occurs
   */
  public ShapefileSchema(File directory, RelDataTypeFactory typeFactory) throws IOException {
    this.directory = Objects.requireNonNull(directory, "Directory cannot be null");
    this.typeFactory = Objects.requireNonNull(typeFactory, "Type factory cannot be null");
    this.tableMap = new HashMap<>();

    // Process files in the directory
    File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".shp"));

    if (files != null) {
      for (File file : files) {
        // Extract the base name without extension (e.g., "countries" from "countries.shp")
        String fileName = file.getName();
        String tableName = fileName;

        // Remove all extensions (e.g., "countries.shp" -> "countries")
        while (tableName.contains(".")) {
          int lastDotIndex = tableName.lastIndexOf('.');
          if (lastDotIndex > 0) {
            tableName = tableName.substring(0, lastDotIndex);
          } else {
            break;
          }
        }

        // Create the table with the file reference
        tableMap.put(tableName, createTable(file));
      }
    }
  }

  /**
   * Constructs a ShapefileSchema with a single file.
   *
   * @param file the shapefile
   * @param typeFactory the type factory to use for creating tables
   * @throws IOException if an I/O error occurs
   */
  public ShapefileSchema(File file, RelDataTypeFactory typeFactory, boolean isDirectory)
      throws IOException {
    if (isDirectory) {
      // If isDirectory is true, treat the file as a directory
      this.directory = Objects.requireNonNull(file, "Directory cannot be null");
      this.typeFactory = Objects.requireNonNull(typeFactory, "Type factory cannot be null");
      this.tableMap = new HashMap<>();

      // Process files in the directory
      File[] files = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".shp"));

      if (files != null) {
        for (File shapeFile : files) {
          // Extract the base name without extension (e.g., "countries" from "countries.shp")
          String fileName = shapeFile.getName();
          String tableName = fileName;

          // Remove all extensions (e.g., "countries.shp" -> "countries")
          while (tableName.contains(".")) {
            int lastDotIndex = tableName.lastIndexOf('.');
            if (lastDotIndex > 0) {
              tableName = tableName.substring(0, lastDotIndex);
            } else {
              break;
            }
          }

          // Create the table with the file reference
          tableMap.put(tableName, createTable(shapeFile));
        }
      }
    } else {
      // If isDirectory is false, treat the file as a single file
      this.directory = Objects.requireNonNull(file, "File cannot be null");
      this.typeFactory = Objects.requireNonNull(typeFactory, "Type factory cannot be null");
      this.tableMap = new HashMap<>();

      // Extract the base name without extension (e.g., "countries" from "countries.shp")
      String fileName = file.getName();
      String tableName = fileName;

      // Remove all extensions (e.g., "countries.shp" -> "countries")
      while (tableName.contains(".")) {
        int lastDotIndex = tableName.lastIndexOf('.');
        if (lastDotIndex > 0) {
          tableName = tableName.substring(0, lastDotIndex);
        } else {
          break;
        }
      }

      // Create the table with the file reference
      tableMap.put(tableName, createTable(file));
    }
  }

  /**
   * Creates a table for the given file.
   *
   * @param file the shapefile
   * @return the created table
   * @throws IOException if an I/O error occurs
   */
  private Table createTable(File file) throws IOException {
    return new ShapefileTable(file);
  }

  @Override
  protected Map<String, Table> getTableMap() {
    return tableMap;
  }
}
