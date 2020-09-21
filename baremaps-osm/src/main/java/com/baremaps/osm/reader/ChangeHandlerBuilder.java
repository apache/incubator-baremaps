package com.baremaps.osm.reader;

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.function.Consumer;

public class ChangeHandlerBuilder {

  private Consumer<Node> nodeCreateConsumer;
  private Consumer<Node> nodeModifyConsumer;
  private Consumer<Node> nodeDeleteConsumer;

  private Consumer<Way> wayCreateConsumer;
  private Consumer<Way> wayModifyConsumer;
  private Consumer<Way> wayDeleteConsumer;

  private Consumer<Relation> relationCreateConsumer;
  private Consumer<Relation> relationModifyConsumer;
  private Consumer<Relation> relationDeleteConsumer;


  public ChangeHandlerBuilder onNodeCreate(Consumer<Node> nodeCreateConsumer) {
    this.nodeCreateConsumer = nodeCreateConsumer;
    return this;
  }

  public ChangeHandlerBuilder onNodeModify(Consumer<Node> nodeModifyConsumer) {
    this.nodeModifyConsumer = nodeModifyConsumer;
    return this;
  }

  public ChangeHandlerBuilder onNodeDelete(Consumer<Node> nodeDeleteConsumer) {
    this.nodeDeleteConsumer = nodeDeleteConsumer;
    return this;
  }

  public ChangeHandlerBuilder onWayCreate(Consumer<Way> wayCreateConsumer) {
    this.wayCreateConsumer = wayCreateConsumer;
    return this;
  }

  public ChangeHandlerBuilder onWayModify(Consumer<Way> wayModifyConsumer) {
    this.wayModifyConsumer = wayModifyConsumer;
    return this;
  }

  public ChangeHandlerBuilder onWayDelete(Consumer<Way> wayDeleteConsumer) {
    this.wayDeleteConsumer = wayDeleteConsumer;
    return this;
  }

  public ChangeHandlerBuilder onRelationCreate(Consumer<Relation> relationCreateConsumer) {
    this.relationCreateConsumer = relationCreateConsumer;
    return this;
  }

  public ChangeHandlerBuilder onRelationModify(Consumer<Relation> relationModifyConsumer) {
    this.relationModifyConsumer = relationModifyConsumer;
    return this;
  }

  public ChangeHandlerBuilder onRelationDelete(Consumer<Relation> relationDeleteConsumer) {
    this.relationDeleteConsumer = relationDeleteConsumer;
    return this;
  }

  public ChangeHandler build() {
    return new ChangeHandler() {
      @Override
      public void onNodeCreate(Node node) {
        if (nodeCreateConsumer != null) {
          nodeCreateConsumer.accept(node);
        }
      }

      @Override
      public void onNodeModify(Node node) {
        if (nodeModifyConsumer != null) {
          nodeModifyConsumer.accept(node);
        }
      }

      @Override
      public void onNodeDelete(Node node) {
        if (nodeDeleteConsumer != null) {
          nodeDeleteConsumer.accept(node);
        }
      }

      @Override
      public void onWayCreate(Way way) {
        if (wayCreateConsumer != null) {
          wayCreateConsumer.accept(way);
        }
      }

      @Override
      public void onWayModify(Way way) {
        if (wayModifyConsumer != null) {
          wayModifyConsumer.accept(way);
        }
      }

      @Override
      public void onWayDelete(Way way) {
        if (wayDeleteConsumer != null) {
          wayDeleteConsumer.accept(way);
        }
      }

      @Override
      public void onRelationCreate(Relation relation) {
        if (relationCreateConsumer != null) {
          relationCreateConsumer.accept(relation);
        }
      }

      @Override
      public void onRelationModify(Relation relation) {
        if (relationModifyConsumer != null) {
          relationModifyConsumer.accept(relation);
        }
      }

      @Override
      public void onRelationDelete(Relation relation) {
        if (relationDeleteConsumer != null) {
          relationDeleteConsumer.accept(relation);
        }
      }
    };
  }

}
