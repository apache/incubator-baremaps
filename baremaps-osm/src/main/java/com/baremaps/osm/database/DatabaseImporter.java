package com.baremaps.osm.database;

import com.baremaps.osm.handler.BlockHandler;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;

public class DatabaseImporter implements BlockHandler {

  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DatabaseImporter(
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void handle(HeaderBlock headerBlock) throws Exception {
    headerTable.insert(headerBlock.getHeader());
  }

  @Override
  public void handle(DataBlock dataBlock) throws Exception {
    nodeTable.copy(dataBlock.getDenseNodes());
    nodeTable.copy(dataBlock.getNodes());
    wayTable.copy(dataBlock.getWays());
    relationTable.copy(dataBlock.getRelations());
  }
}
