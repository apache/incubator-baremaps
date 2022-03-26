package com.baremaps.core;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Test {

  public void execute() {

    CompletableFuture<Void> osmWater = CompletableFuture
        .completedFuture(new Source())
        .thenApply(this::download)
        .thenApply(this::transform)
        .thenAccept(this::save);



  }

  private Dataset download(Source source) {
    return null;
  }

  private Dataset transform(Dataset dataset) {
    return null;
  }

  private void save(Dataset dataset) {
  }


  public class Source {

  }

  public class Dataset {

  }


}
