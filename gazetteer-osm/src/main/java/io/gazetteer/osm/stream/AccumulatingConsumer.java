package io.gazetteer.osm.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@code Consumer} that accumulates the values it accepted.
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
   * @return
   */
  public List<T> values() {
    return values;
  }
}
