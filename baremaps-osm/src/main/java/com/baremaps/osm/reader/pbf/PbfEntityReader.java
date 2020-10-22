package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.EntityHandler;
import com.baremaps.osm.reader.EntityReader;
import com.baremaps.osm.reader.ReaderException;
import java.nio.file.Path;

public class PbfEntityReader implements EntityReader {

  public void read(Path path, EntityHandler handler) throws ReaderException {
    FileBlockReader reader = new FileBlockReader();
    reader.read(path, new FileBlockHandler() {
      @Override
      public void onHeaderBlock(HeaderBlock header) throws Exception {
        handler.onHeader(header.getHeader());
      }

      @Override
      public void onDataBlock(DataBlock dataBlock) throws Exception {
        for (Node node : dataBlock.getDenseNodes()) {
          handler.onNode(node);
        }
        for (Node node : dataBlock.getNodes()) {
          handler.onNode(node);
        }
        for (Way way : dataBlock.getWays()) {
          handler.onWay(way);
        }
        for (Relation relation : dataBlock.getRelations()) {
          handler.onRelation(relation);
        }
      }
    });
  }

}
