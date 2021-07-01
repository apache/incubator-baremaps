package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Change;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

/**
 * Represents an operation on changes of different types.
 */
public interface ChangeConsumer extends Consumer<Change> {

  @Override
  default void accept(Change change) {
    try {
      change.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void match(Change change) throws Exception;

}
