package com.baremaps.core.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@code Consumer} that accumulates the values it accepts.
 *
 * @param <T>
 */
public class AccumulatingConsumer<T> implements Consumer<T> {

  private List<T> values = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(T value) {
    values.add(value);
  }

  /**
   * Returns the accumulated values.
   *
   * @return the accumulated values.
   */
  public List<T> values() {
    return values;
  }
}
