package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Change;
import com.baremaps.stream.StreamException;
import java.util.function.Function;

/**
 * Represents a function that transforms entities of different types.
 *
 * @param <T>
 */
public interface ChangeFunction<T> extends Function<Change, T> {

  @Override
  default T apply(Change change) {
    try {
      return change.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  T match(Change change) throws Exception;

}

