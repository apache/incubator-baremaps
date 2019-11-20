package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.model.Entry;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.util.stream.Collectors;

public class CopyConsumer extends FileBlockConsumer {

  private final PostgisHeaderStore headerMapper;
  private final PostgisNodeStore nodeMapper;
  private final PostgisWayStore wayMapper;
  private final PostgisRelationStore relationMapper;

  public CopyConsumer(PostgisHeaderStore headerMapper, PostgisNodeStore nodeMapper, PostgisWayStore wayMapper, PostgisRelationStore relationMapper) {
    this.headerMapper = headerMapper;
    this.nodeMapper = nodeMapper;
    this.wayMapper = wayMapper;
    this.relationMapper = relationMapper;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try {
      headerMapper.insert(headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      nodeMapper.importAll(primitiveBlock.getDenseNodes().stream()
          .map(node -> new Entry<>(node.getInfo().getId(), node))
          .collect(Collectors.toList()));
      wayMapper.importAll(primitiveBlock.getWays().stream()
          .map(way -> new Entry<>(way.getInfo().getId(), way))
          .collect(Collectors.toList()));
      relationMapper.importAll(primitiveBlock.getRelations().stream()
          .map(relation -> new Entry<>(relation.getInfo().getId(), relation))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
