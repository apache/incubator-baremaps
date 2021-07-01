package com.baremaps.osm.database;

import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.BlockConsumerAdapter;

class DatabaseImportConsumer implements BlockConsumerAdapter {

  private EntityTable<Header> headerTable;
  private EntityTable<Node> nodeTable;
  private EntityTable<Way> wayTable;
  private EntityTable<Relation> relationTable;

  public DatabaseImportConsumer(
      EntityTable<Header> headerTable,
      EntityTable<Node> nodeTable,
      EntityTable<Way> wayTable,
      EntityTable<Relation> relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void match(HeaderBlock headerBlock) throws Exception {
    headerTable.insert(headerBlock.getHeader());
  }

  @Override
  public void match(DataBlock dataBlock) throws Exception {
    nodeTable.copy(dataBlock.getDenseNodes());
    nodeTable.copy(dataBlock.getNodes());
    wayTable.copy(dataBlock.getWays());
    relationTable.copy(dataBlock.getRelations());
  }
}
