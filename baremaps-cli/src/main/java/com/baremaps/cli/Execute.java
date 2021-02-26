package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.postgres.PostgresHelper;
import com.google.common.base.Splitter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "execute", description = "Execute queries in the database.")
public class Execute implements Callable<Integer> {

  private static Logger logger = LoggerFactory.getLogger(Execute.class);

  @Mixin
  private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--file"},
      paramLabel = "FILE",
      description = "The SQL file to execute in the database.",
      required = true)
  private List<URI> files;

  @Option(
      names = {"--parallel"},
      paramLabel = "PARALLEL",
      negatable = true,
      description = "Enable parallel execution of queries.")
  public boolean parallel = true;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());
    DataSource datasource = PostgresHelper.datasource(database);
    BlobStore blobStore = options.blobStore();

    for (URI file : files) {
      logger.info("{}", file);
      String blob = new String(blobStore.readByteArray(file), StandardCharsets.UTF_8);
      Stream<String> queries = Splitter.on(";").splitToStream(blob);
      if (parallel) {
        queries = queries.parallel();
      }
      queries.parallel().forEach(query -> {
        try (Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement()) {
          statement.execute(query);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      });
    }

    return 0;
  }

}
