/*
 * Copyright (C) 2020 The baremaps Authors
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
package com.baremaps.osm.parser;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.util.stream.StreamException;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

public class PBFEntityParser {

  public void parse(Path path, PBFEntityHandler handler) throws Exception {
    new PBFFileBlockParser().parse(path, new PBFFileBlockHandler() {
      @Override
      public void onHeader(Header header) throws Exception {
        handler.onHeader(header);
      }

      @Override
      public void onNodes(List<Node> nodes) throws Exception {
        for (Node node : nodes) {
          handler.onNode(node);
        }
      }

      @Override
      public void onWays(List<Way> ways) throws Exception {
        for (Way way : ways) {
          handler.onWay(way);
        }
      }

      @Override
      public void onRelations(List<Relation> relations) throws Exception {
        for (Relation relation : relations) {
          handler.onRelation(relation);
        }
      }

      @Override
      public void onComplete() throws Exception {
        handler.onComplete();
      }

      @Override
      public void onError(Throwable error) throws Exception {
        handler.onError(error);
      }
    });
  }

}
