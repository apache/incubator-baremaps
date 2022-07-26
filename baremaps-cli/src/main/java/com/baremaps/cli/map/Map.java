package com.baremaps.cli.map;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "map",
    description = "Map commands.",
    subcommands = {
        Init.class,
        Export.class,
        Serve.class,
        Dev.class
    },
    sortOptions = false)
public class Map implements Runnable{


  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }
}
