package com.baremaps.osm;

import com.baremaps.osm.domain.Change;
import com.baremaps.util.stream.StreamException;
import java.util.function.Consumer;

public interface ChangeHandler extends Consumer<Change> {

  @Override
  default void accept(Change change) {
    try {
      handle(change);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void handle(Change change) throws Exception;

}
