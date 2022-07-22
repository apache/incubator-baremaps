package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import com.baremaps.workflow.tasks.UpdateOpenStreetMap;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "update", description = "Update OpenStreetMap data in Postgres.")
public class Update implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Update.class);

  @Mixin private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of Postgres.",
      required = true)
  private String database;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public void run() {
    new UpdateOpenStreetMap(
        database,
        srid
    ).run();
  }
}