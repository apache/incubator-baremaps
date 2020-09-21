/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.osm.reader.xml;

import com.baremaps.osm.model.Change;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.ChangeHandler;
import com.baremaps.osm.reader.ReaderException;
import com.baremaps.osm.stream.StreamException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XmlChangeReader {

  protected InputStream open(Path path) throws IOException {
    return Files.newInputStream(path);
  }

  public void parse(Path path, ChangeHandler handler) throws Exception {
    try (InputStream changeInputStream = open(path)) {
      Spliterator<Change> spliterator = new XmlChangeSpliterator(changeInputStream);
      Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
      changeStream.forEach(change -> {
        try {
          parseChange(change, handler);
        } catch (Exception e) {
          throw new StreamException(e);
        }
      });
    } catch (StreamException | IOException e) {
      throw new ReaderException(e.getCause());
    }
  }

  private void parseChange(Change change, ChangeHandler handler) throws Exception {
    Entity entity = change.getEntity();
    if (entity instanceof Node) {
      Node node = (Node) entity;
      switch (change.getType()) {
        case create:
          handler.onNodeCreate(node);
          break;
        case modify:
          handler.onNodeModify(node);
          break;
        case delete:
          handler.onNodeDelete(node);
          break;
        default:
          break;
      }
    } else if (entity instanceof Way) {
      Way way = (Way) entity;
      switch (change.getType()) {
        case create:
          handler.onWayCreate(way);
          break;
        case modify:
          handler.onWayModify(way);
          break;
        case delete:
          handler.onWayDelete(way);
          break;
        default:
          break;
      }
    } else if (entity instanceof Relation) {
      Relation relation = (Relation) entity;
      switch (change.getType()) {
        case create:
          handler.onRelationCreate(relation);
          break;
        case modify:
          handler.onRelationModify(relation);
          break;
        case delete:
          handler.onRelationDelete(relation);
          break;
        default:
          break;
      }
    }
  }

}
