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

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import daylight features.
 */
public class ImportDaylightFeatures implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportDaylightFeatures.class);

  record Feature(
      @JsonProperty("osm_type") String type,
      @JsonProperty("osm_id") Long id,
      @JsonProperty("tags") Map<String, Object> tags,
      @JsonProperty("wkt") String wkt,
      @JsonProperty("category") String category) {
  }

  private Path file;

  private Object database;

  /**
   * Constructs an {@code ImportDaylightFeatures}.
   */
  public ImportDaylightFeatures() {

  }

  /**
   * Constructs an {@code ImportDaylightFeatures}.
   *
   * @param file the daylight file
   * @param database the database
   */
  public ImportDaylightFeatures(Path file, Object database) {
    this.file = file;
    this.database = database;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);

    // Initialize the repositories
    var nodeRepository = new PostgresNodeRepository(datasource);
    var wayRepository = new PostgresWayRepository(datasource);
    var relationRepository = new PostgresRelationRepository(datasource);
    nodeRepository.create();
    wayRepository.create();
    relationRepository.create();

    // Process the file
    var objectMapper = new ObjectMapper();
    var javaType = objectMapper.getTypeFactory().constructParametricType(List.class, Feature.class);
    List<Feature> features = objectMapper.readValue(file.toFile(), javaType);
    for (var feature : features) {
      switch (feature.type()) {
        case "node" -> {
          var node = nodeRepository.get(feature.id());
          if (node != null) {
            var tags = new HashMap<>(feature.tags());
            if (node.getTags() != null) {
              tags.putAll(node.getTags());
            }
            node.setTags(tags);
            nodeRepository.put(node);
          }
        }
        case "way" -> {
          var way = wayRepository.get(feature.id());
          if (way != null) {
            var tags = new HashMap<>(feature.tags());
            if (way.getTags() != null) {
              tags.putAll(way.getTags());
            }
            way.setTags(tags);
            wayRepository.put(way);
          }
        }
        case "relation" -> {
          var relation = relationRepository.get(feature.id());
          if (relation != null) {
            var tags = new HashMap<>(feature.tags());
            if (relation.getTags() != null) {
              tags.putAll(relation.getTags());
            }
            relation.setTags(tags);
            relationRepository.put(relation);
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", ImportDaylightFeatures.class.getSimpleName() + "[", "]")
        .add("file=" + file)
        .add("database=" + database)
        .toString();
  }
}
