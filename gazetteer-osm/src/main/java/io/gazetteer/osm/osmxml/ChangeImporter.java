package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.ChangeUtil.statePath;

import io.gazetteer.common.io.URLUtil;
import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.postgis.HeaderTable;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import javax.sql.DataSource;

public class ChangeImporter implements Callable<Integer> {

  private final DataSource datasource;

  private final ExecutorService executor;

  public ChangeImporter(DataSource datasource, ExecutorService executor) {
    this.datasource = datasource;
    this.executor = executor;
  }

  @Override
  public Integer call() throws Exception {
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

    return 1;
  }

}
