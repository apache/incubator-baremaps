package io.gazetteer.common.stream;

import java.util.function.Consumer;

public class HoldingConsumer<T> implements Consumer<T> {

  public T value;

  @Override
  public void accept(T value) {
    this.value = value;
  }

}
