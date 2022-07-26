package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import java.util.concurrent.Callable;
import org.jdbi.v3.core.statement.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
    name = "update-osm",
    description = "Update OpenStreetMap data in Postgres.")
public class UpdateOpenStreetMap implements Callable<Integer> {

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
  public Integer call() throws Exception {
    new com.baremaps.workflow.tasks.UpdateOpenStreetMap(
        database,
        srid
    ).run();
    return 0;
  }
}