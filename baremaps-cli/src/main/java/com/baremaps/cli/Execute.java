package com.baremaps.cli;

import com.baremaps.util.postgres.PostgresHelper;
import com.baremaps.util.storage.BlobStore;
import com.google.common.base.Splitter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "execute", description = "Execute queries in the database.")
public class Execute implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  @Mixin
  private Options options;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--queries"},
      paramLabel = "PBF",
      description = "The queries to execute in the database.",
      required = true)
  private URI queries;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(options.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());
    PoolingDataSource datasource = PostgresHelper.poolingDataSource(database);
    BlobStore blobStore = options.blobStore();

    logger.info("Execute queries");
    String rawQueries = new String(blobStore.readByteArray(queries), StandardCharsets.UTF_8);
    Splitter.on(";").splitToStream(rawQueries).parallel().forEach(query -> {
        try (Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement()) {
          statement.execute(query);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
    });

    return 0;
  }

}
