package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
    name = "import-osm",
    description = "Import OpenStreetMap data in Postgres.")
public class ImportOpenStreetMap implements Callable<Integer> {

  @Mixin
  private Options options;

  @Option(
      names = {"--file"},
      paramLabel = "FILE",
      description = "The PBF file to import in the database.",
      required = true)
  private Path file;

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
    new com.baremaps.workflow.tasks.ImportOpenStreetMap(
        file.toAbsolutePath().toString(),
        database,
        srid
    ).run();
    return 0;
  }
}
