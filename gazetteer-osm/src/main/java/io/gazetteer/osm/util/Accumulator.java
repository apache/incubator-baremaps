package io.gazetteer.osm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Accumulator<E> implements Consumer<E> {

  public List<E> acc = new ArrayList<>();

  @Override
  public void accept(E e) {
    acc.add(e);
  }
}
