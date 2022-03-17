package com.baremaps.collection;

import java.io.IOException;

/**
 * A {@link Cleanable} is a data object that can be cleaned.
 * The clean method is invoked to delete resources that the object is using.
 */
public interface Cleanable {

  /**
   * Cleans the underlying resources (files, memory, etc.) used by this object.
   */
  void clean() throws IOException;

}
