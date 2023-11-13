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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDaylightTranslations implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportDaylightTranslations.class);

  record Group(String type, Long id, String name) {

  }

  record Line(String type, Long id, String name, String attributeKey, String attributeValue) {

    public Group group() {
      return new Group(type, id, name);
    }

    public static Line parse(String line) {
      var parts = line.split("\t");
      var type = parts[0];
      var id = Long.parseLong(parts[1]);
      var name = parts[2];
      var key = parts[3];
      var val = parts[4];
      return new Line(type, id, name, key, val);
    }
  }


  private Path file;

  private Object database;

  public ImportDaylightTranslations() {

  }

  public ImportDaylightTranslations(Path file, Object database) {
    this.file = file;
    this.database = database;
  }

  public Path getFile() {
    return file;
  }

  public void setFile(Path file) {
    this.file = file;
  }

  public Object getDatabase() {
    return database;
  }

  public void setDatabase(Object database) {
    this.database = database;
  }

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
    try (var lines = Files.lines(file)) {
      var entries = lines.map(Line::parse).collect(Collectors.groupingBy(Line::group));
      for (var entry : entries.entrySet()) {
        var group = entry.getKey();
        switch (group.type()) {
          case "node" -> {
            var node = nodeRepository.get(group.id());
            if (node != null) {
              var tags = new HashMap<>(node.getTags());
              for (var line : entry.getValue()) {
                tags.put(line.attributeKey(), line.attributeValue());
              }
              node.setTags(tags);
              nodeRepository.put(node);
            }
          }
          case "way" -> {
            var way = wayRepository.get(group.id());
            if (way != null) {
              var tags = new HashMap<>(way.getTags());
              for (var line : entry.getValue()) {
                tags.put(line.attributeKey(), line.attributeValue());
              }
              way.setTags(tags);
              wayRepository.put(way);
            }
          }
          case "relation" -> {
            var relation = relationRepository.get(group.id());
            if (relation != null) {
              var tags = new HashMap<>(relation.getTags());
              for (var line : entry.getValue()) {
                tags.put(line.attributeKey(), line.attributeValue());
              }
              relation.setTags(tags);
              relationRepository.put(relation);
            }
          }
        }
      }
    }
  }
}
