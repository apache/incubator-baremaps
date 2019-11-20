package io.gazetteer.cli.commands;

import static io.gazetteer.osm.osmxml.ChangeUtil.statePath;
import static org.lmdbjava.DbiFlags.MDB_CREATE;

import io.gazetteer.cli.util.StopWatch;
import io.gazetteer.common.io.URLUtil;
import io.gazetteer.common.postgis.DatabaseUtils;
import io.gazetteer.osm.geometry.NodeGeometryBuilder;
import io.gazetteer.osm.geometry.RelationGeometryBuilder;
import io.gazetteer.osm.geometry.WayGeometryBuilder;
import io.gazetteer.osm.lmdb.LmdbCoordinateStore;
import io.gazetteer.osm.lmdb.LmdbReferenceStore;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmxml.Change;
import io.gazetteer.osm.osmxml.ChangeConsumer;
import io.gazetteer.osm.osmxml.ChangeUtil;
import io.gazetteer.osm.osmxml.State;
import io.gazetteer.osm.osmxml.StateUtil;
import io.gazetteer.osm.postgis.PostgisCoordinateStore;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisReferenceStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update")
public class Update implements Callable<Integer> {

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
    PoolingDataSource datasource = DatabaseUtils.poolingDataSource(database);
    try {
      Store<Long, Coordinate> coordinateStore = new PostgisCoordinateStore(datasource);
      Store<Long, List<Long>> referenceStore = new PostgisReferenceStore(datasource);
      GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
      PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
      PostgisNodeStore nodeStore = new PostgisNodeStore(datasource, new NodeGeometryBuilder(geometryFactory));
      PostgisWayStore wayStore = new PostgisWayStore(datasource, new WayGeometryBuilder(geometryFactory, coordinateStore));
      PostgisRelationStore relationStore = new PostgisRelationStore(datasource, new RelationGeometryBuilder(geometryFactory, coordinateStore, referenceStore));

      HeaderBlock header = headerMapper.last();
      long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;
      String statePath = statePath(nextSequenceNumber);
      URL stateURL = new URL(String.format("%s/%s", header.getReplicationUrl(), statePath));
      String stateContent = URLUtil.toString(stateURL);
      State state = StateUtil.parse(stateContent);

      String changePath = ChangeUtil.changePath(nextSequenceNumber);
      URL changeURL = new URL(String.format("%s/%s", header.getReplicationUrl(), changePath));
      try (InputStream changeInputStream = URLUtil.toGZIPInputStream(changeURL)) {
        Stream<Change> changeStream = ChangeUtil.stream(changeInputStream);
        ChangeConsumer changeConsumer = new ChangeConsumer(datasource, nodeStore, wayStore, relationStore);
        executor.submit(() -> changeStream.forEach(changeConsumer)).get();
      }

      headerMapper.insert(new HeaderBlock(
          state.timestamp,
          state.sequenceNumber,
          header.getReplicationUrl(),
          header.getSource(),
          header.getWritingProgram(),
          header.getBbox()));
      return 0;
    } finally {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }

}
