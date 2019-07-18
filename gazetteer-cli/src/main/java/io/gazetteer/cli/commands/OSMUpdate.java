package io.gazetteer.cli.commands;

import static io.gazetteer.osm.osmxml.ChangeUtil.statePath;

import io.gazetteer.common.cli.StopWatch;
import io.gazetteer.common.io.URLUtil;
import io.gazetteer.common.postgis.DatabaseUtil;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmxml.Change;
import io.gazetteer.osm.osmxml.ChangeUtil;
import io.gazetteer.osm.osmxml.State;
import io.gazetteer.osm.osmxml.StateUtil;
import io.gazetteer.osm.postgis.ChangeConsumer;
import io.gazetteer.osm.postgis.HeaderTable;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name="update")
public class OSMUpdate implements Callable<Integer> {

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
    StopWatch stopWatch = new StopWatch();
    ForkJoinPool executor = new ForkJoinPool(threads);
    PoolingDataSource datasource = DatabaseUtil.poolingDataSource(database);
    try {
      HeaderBlock header;
      try (Connection connection = datasource.getConnection()) {
        header = HeaderTable.last(connection);
      }

      long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;

      String statePath = statePath(nextSequenceNumber);
      URL stateURL = new URL(String.format("%s/%s", header.getReplicationUrl(), statePath));
      String stateContent = URLUtil.toString(stateURL);
      State state = StateUtil.parse(stateContent);

      String changePath = ChangeUtil.changePath(nextSequenceNumber);
      URL changeURL = new URL(String.format("%s/%s", header.getReplicationUrl(), changePath));
      try (InputStream changeInputStream = URLUtil.toGZIPInputStream(changeURL)) {
        Stream<Change> changeStream = ChangeUtil.stream(changeInputStream);
        ChangeConsumer changeConsumer = new ChangeConsumer(datasource);
        executor.submit(() -> changeStream.forEach(changeConsumer)).get();
      }

      try (Connection connection = datasource.getConnection()) {
        HeaderTable.insert(connection,
            new HeaderBlock(state.timestamp, state.sequenceNumber, header.getReplicationUrl(), header.getSource(), header.getWritingProgram(),
                header.getBbox()));
      }

      return 0;
    } finally {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }

}