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

package org.apache.baremaps.calcite.csv;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * A Calcite schema implementation for CSV data. This schema provides access to CSV files through
 * the Apache Calcite framework for SQL querying.
 */
public class CsvSchema extends AbstractSchema {

  private final File directory;
  private final Map<String, Table> tableMap;
  private final char separator;
  private final boolean hasHeader;

  /**
   * Constructs a CsvSchema with the specified directory.
   *
   * @param directory the directory containing CSV files
   * @param separator the separator character used in CSV files
   * @param hasHeader whether CSV files have headers
   * @throws IOException if an I/O error occurs
   */
  public CsvSchema(File directory, char separator, boolean hasHeader) throws IOException {
    this.directory = directory;
    this.separator = separator;
    this.hasHeader = hasHeader;
    this.tableMap = new HashMap<>();

    // Only process .csv files in the directory
    File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
    if (files != null) {
      for (File file : files) {
        String tableName = file.getName().replaceFirst("[.][^.]+$", ""); // Remove extension
        tableMap.put(tableName, new CsvTable(file, separator, hasHeader));
      }
    }
  }

  @Override
  protected Map<String, Table> getTableMap() {
    return tableMap;
  }
}
