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

package org.apache.baremaps.workflow;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.apache.baremaps.data.collection.*;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.data.type.*;
import org.apache.baremaps.data.type.geometry.LonLatDataType;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.utils.PostgresUtils;
import org.locationtech.jts.geom.Coordinate;

/**
 * A context that is passed to the tasks of a workflow and used to share data between tasks.
 */
public class WorkflowContext {

  private final Path dataDir;

  private final Path cacheDir;

  public WorkflowContext() {
    this(Paths.get("./data"), Paths.get("./cache"));
  }

  public WorkflowContext(Path dataDir, Path cacheDir) {
    this.dataDir = dataDir;
    this.cacheDir = cacheDir;
  }

  private Map<Object, DataSource> dataSources = new ConcurrentHashMap<>();

  /**
   * Returns the data source associated with the specified database.
   *
   * @param database the JDBC connection string to the database
   * @return the data source
   */
  public DataSource getDataSource(Object database) {
    return dataSources.computeIfAbsent(database, PostgresUtils::createDataSourceFromObject);
  }

  public Map<Long, Coordinate> getCoordinateMap() throws IOException {
    return DataConversions.asMap(getMemoryAlignedDataMap("coordinates", new LonLatDataType()));
  }

  public Map<Long, List<Long>> getReferenceMap() throws IOException {
    return DataConversions.asMap(getMonotonicDataMap("references", new LongListDataType()));
  }

  public <T> DataMap<Long, T> getMemoryAlignedDataMap(String name, FixedSizeDataType<T> dataType)
      throws IOException {
    var coordinateDir = Files.createDirectories(cacheDir.resolve(name));
    return new MemoryAlignedDataMap<>(
        dataType,
        new MemoryMappedDirectory(coordinateDir));
  }

  public <T> DataMap<Long, T> getMonotonicDataMap(String name, DataType<T> dataType)
      throws IOException {
    var mapDir = Files.createDirectories(cacheDir.resolve(name));
    var keysDir = Files.createDirectories(mapDir.resolve("keys"));
    var valuesDir = Files.createDirectories(mapDir.resolve("values"));
    return new MonotonicDataMap<>(
        new MemoryAlignedDataList<>(
            new PairDataType<>(new LongDataType(), new LongDataType()),
            new MemoryMappedDirectory(keysDir)),
        new AppendOnlyLog<>(
            dataType,
            new MemoryMappedDirectory(valuesDir)));
  }

  public void cleanCache() throws IOException {
    FileUtils.deleteRecursively(cacheDir);
  }

  public void cleanData() throws IOException {
    FileUtils.deleteRecursively(dataDir);
  }
}
