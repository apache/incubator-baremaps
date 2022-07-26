package com.baremaps.cli.database;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "database",
    description = "Database commands.",
    subcommands = {
        ExecuteSql.class,
        ImportOpenStreetMap.class,
        UpdateOpenStreetMap.class
    },
    sortOptions = false)
public class Database implements Runnable {

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }
}
