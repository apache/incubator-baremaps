package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import com.baremaps.workflow.tasks.ImportOpenStreetMap;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "import", description = "Import OpenStreetMap data in Postgres.")
public class Import implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

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
  public void run()  {
    new ImportOpenStreetMap(
        file.toAbsolutePath().toString(),
        database,
        srid
    ).run();
  }
}
