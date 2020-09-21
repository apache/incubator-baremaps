package com.baremaps.osm.reader;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.function.Consumer;

public class EntityHandlerBuilder {

  private Consumer<Header> headerHandler;

  private Consumer<Node> nodeHandler;

  private Consumer<Way> wayHandler;

  private Consumer<Relation> relationHandler;

  public EntityHandlerBuilder onHeader(Consumer<Header> headerHandler) {
    this.headerHandler = headerHandler;
    return this;
  }

  public EntityHandlerBuilder onNode(Consumer<Node> nodeHandler) {
    this.nodeHandler = nodeHandler;
    return this;
  }

  public EntityHandlerBuilder onWay(Consumer<Way> wayHandler) {
    this.wayHandler = wayHandler;
    return this;
  }

  public EntityHandlerBuilder onRelation(Consumer<Relation> relationHandler) {
    this.relationHandler = relationHandler;
    return this;
  }

  public EntityHandler build() {
    return new EntityHandler() {
      @Override
      public void onHeader(Header header) {
        if (headerHandler != null) {
          headerHandler.accept(header);
        }
      }

      @Override
      public void onNode(Node node) {
        if (nodeHandler != null) {
          nodeHandler.accept(node);
        }
      }

      @Override
      public void onWay(Way way) {
        if (wayHandler != null) {
          wayHandler.accept(way);
        }
      }

      @Override
      public void onRelation(Relation relation) {
        if (relationHandler != null) {
          relationHandler.accept(relation);
        }
      }
    };
  }

}
