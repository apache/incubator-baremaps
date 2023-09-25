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

package org.apache.baremaps.storage.shapefile;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.baremaps.database.schema.DataSchema;
import org.apache.baremaps.database.schema.DataTable;
import org.apache.baremaps.database.schema.DataTableException;

/**
 * A schema corresponding to the shapefiles of a directory.
 */
public class ShapefileDataSchema implements DataSchema {

  private final Path directory;

  /**
   * Constructs a schema from a directory.
   *
   * @param directory the directory
   */
  public ShapefileDataSchema(Path directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> list() {
    try (var files = Files.list(directory)) {
      return files
          .filter(file -> file.toString().toLowerCase().endsWith(".shp"))
          .map(file -> file.getFileName().toString())
          .toList();
    } catch (IOException e) {
      throw new DataTableException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) {
    return new ShapefileDataTable(directory.resolve(name));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable value) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) {
    throw new UnsupportedOperationException();
  }
}
