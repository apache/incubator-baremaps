package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileBlockGeometryStreamer {

  private final NodeBuilder nodeBuilder;

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationBuilder;

  private final FileBlockHandler handler;

  public FileBlockGeometryStreamer(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      FileBlockHandler handler) {
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationBuilder = relationBuilder;
    this.handler = handler;
  }

  public Stream<FileBlock> stream(Path path, boolean parallel, boolean progress) throws IOException {
    return new FileBlockStreamer()
        .stream(path, parallel, progress)
        .peek(this::withGeometry);
  }

  private void withGeometry(FileBlock fileBlock) {
    if (fileBlock instanceof DataBlock) {
      DataBlock dataBlock = (DataBlock) fileBlock;
      for (Node node : dataBlock.getDenseNodes()) {
        node.setGeometry(nodeBuilder.build(node));
      }
      List<Node> nodes = dataBlock.getNodes();
      for (Node node: nodes) {
        node.setGeometry(nodeBuilder.build(node));
      }
      List<Way> ways = dataBlock.getWays();
      for (Way way : ways) {
        way.setGeometry(wayBuilder.build(way));
      }
      List<Relation> relations = dataBlock.getRelations();
      for (Relation relation : relations) {
        relation.setGeometry(relationBuilder.build(relation));
      }
    }
  }

}
