package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.EntityHandler;
import com.baremaps.osm.reader.EntityReader;
import com.baremaps.osm.reader.ReaderException;
import java.nio.file.Path;
import java.util.List;

public class PbfEntityReader implements EntityReader {

  public void read(Path path, EntityHandler handler) throws ReaderException {
    FileBlockReader reader = new FileBlockReader();
    reader.read(path, new FileBlockHandler() {
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
    });
  }

}
