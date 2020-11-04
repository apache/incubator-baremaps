package com.baremaps.importer.database;

import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.domain.Bounds;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.stream.StreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataImporter implements ElementHandler, AutoCloseable {

  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  private final Map<Thread, List<Node>> nodeBuffers = new ConcurrentHashMap<>();
  private final Map<Thread, List<Way>> wayBuffers = new ConcurrentHashMap<>();
  private final Map<Thread, List<Relation>> relationBuffers = new ConcurrentHashMap<>();

  public DataImporter(
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void handle(Node node) {
    try {
      List<Node> buffer = nodeBuffers.computeIfAbsent(Thread.currentThread(), thread -> new ArrayList<>());
      buffer.add(node);
      if (buffer.size() == 1000) {
        nodeTable.copy(buffer);
        buffer.clear();
      }
    } catch (DatabaseException e) {
      throw new StreamException(e);
    }
  }

  @Override
  public void handle(Way way) {
    try {
      List<Way> buffer = wayBuffers.computeIfAbsent(Thread.currentThread(), thread -> new ArrayList<>());
      buffer.add(way);
      if (buffer.size() == 1000) {
        wayTable.copy(buffer);
        buffer.clear();
      }
    } catch (DatabaseException e) {
      throw new StreamException(e);
    }
  }

  @Override
  public void handle(Relation relation) {
    try {
      List<Relation> buffer = relationBuffers.computeIfAbsent(Thread.currentThread(), thread -> new ArrayList<>());
      buffer.add(relation);
      if (buffer.size() == 1000) {
        relationTable.copy(buffer);
        buffer.clear();
      }
    } catch (DatabaseException e) {
      throw new StreamException(e);
    }
  }

  @Override
  public void close() {
    try {
      for (List<Node> buffer : nodeBuffers.values()) {
        nodeTable.copy(buffer);
        buffer.clear();
      }
      for (List<Way> buffer : wayBuffers.values()) {
        wayTable.copy(buffer);
        buffer.clear();
      }
      for (List<Relation> buffer : relationBuffers.values()) {
        relationTable.copy(buffer);
        buffer.clear();
      }
    } catch (DatabaseException e) {
      throw new StreamException(e);
    }
  }

}
