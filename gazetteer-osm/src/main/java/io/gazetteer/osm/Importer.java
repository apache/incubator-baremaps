package io.gazetteer.osm;

import static picocli.CommandLine.Option;

import io.gazetteer.common.postgis.DatabaseUtil;
import io.gazetteer.osm.osmxml.ChangeImporter;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(description = "Import OSM PBF into Postgresql")
public class Importer implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF url.")
  private String source;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public Integer call() throws Exception {
    ForkJoinPool executor = new ForkJoinPool(threads);
    PoolingDataSource datasource = DatabaseUtil.poolingDataSource(database);
    try {
      //return new PBFImporter(source, datasource, executor).call();
      return new ChangeImporter(datasource, executor).call();
    } finally {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }

  public static void main(String[] args) {
    CommandLine.call(new Importer(), args);
  }


}
