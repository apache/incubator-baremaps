package com.baremaps.importer.database;

import com.baremaps.osm.BlockHandler;
import com.baremaps.osm.pbf.DataBlock;
import com.baremaps.osm.pbf.HeaderBlock;

public class DatabaseImportHandler implements BlockHandler {

  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DatabaseImportHandler(
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
