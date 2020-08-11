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
package com.baremaps.osm.parser;

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.util.stream.StreamException;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

public class PBFFileBlockParser {

  public void parse(Path path, PBFFileBlockHandler handler) throws ParserException {
    try (DataInputStream data = new DataInputStream(Files.newInputStream(path))) {
      Spliterator<FileBlock> spliterator = new FileBlockSpliterator(data);
      StreamSupport.stream(spliterator, true).forEach(b -> parseBlock(b, handler));
    } catch (StreamException | IOException e) {
      throw new ParserException(e.getCause());
    }
  }

  private void parseBlock(FileBlock fileBlock, PBFFileBlockHandler handler) {
    switch (fileBlock.getType()) {
      case OSMHeader:
        parseHeader(fileBlock, handler);
        break;
      case OSMData:
        parseData(fileBlock, handler);
        break;
      default:
        // skip unknown file block types
        break;
    }
  }

  private void parseHeader(FileBlock fileBlock, PBFFileBlockHandler handler) {
    try {
      handler.onHeader(new HeaderBlockWrapper(fileBlock).getHeader());
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  private void parseData(FileBlock fileBlock, PBFFileBlockHandler handler) {
    try {
      DataBlockWrapper parser = new DataBlockWrapper(fileBlock);
      List<Node> denseNodes = parser.getDenseNodes();
      if (denseNodes.size() > 0) {
        handler.onNodes(denseNodes);
      }
      List<Node> nodes = parser.getNodes();
      if (nodes.size() > 0) {
        handler.onNodes(nodes);
      }
      List<Way> ways = parser.getWays();
      if (ways.size() > 0) {
        handler.onWays(ways);
      }
      List<Relation> relations = parser.getRelations();
      if (relations.size() > 0) {
        handler.onRelations(relations);
      }
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

}
