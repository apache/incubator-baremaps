package io.gazetteer.cli.commands;

import static io.gazetteer.cli.util.IOUtil.url;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.gazetteer.cli.util.StopWatch;
import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.geometry.RelationBuilder;
import io.gazetteer.osm.geometry.WayBuilder;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmxml.Change;
import io.gazetteer.osm.osmxml.ChangeConsumer;
import io.gazetteer.osm.osmxml.ChangeSpliterator;
import io.gazetteer.osm.osmxml.State;
import io.gazetteer.osm.postgis.PostgisHelper;
import io.gazetteer.osm.store.PostgisCoordinateStore;
import io.gazetteer.osm.store.PostgisHeaderStore;
import io.gazetteer.osm.store.PostgisNodeStore;
import io.gazetteer.osm.store.PostgisReferenceStore;
import io.gazetteer.osm.store.PostgisRelationStore;
import io.gazetteer.osm.store.PostgisWayStore;
import io.gazetteer.osm.store.StoreReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update")
public class Update implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "REPLICATION_DIRECTORY", description = "The replication directory.")
  private String source;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The postgres database.")
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public Integer call() throws Exception {
    StopWatch stopWatch = new StopWatch();
    ForkJoinPool executor = new ForkJoinPool(threads);
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);
    try {
      CRSFactory crsFactory = new CRSFactory();
      CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
      CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
      CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
      CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(epsg4326, epsg3857);
      GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
      StoreReader<Long, Coordinate> coordinateStore = new PostgisCoordinateStore(datasource);
      StoreReader<Long, List<Long>> referenceStore = new PostgisReferenceStore(datasource);
      PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
      PostgisNodeStore nodeStore = new PostgisNodeStore(datasource, new NodeBuilder(coordinateTransform, geometryFactory));
      PostgisWayStore wayStore = new PostgisWayStore(datasource, new WayBuilder(coordinateTransform, geometryFactory, coordinateStore));
      PostgisRelationStore relationStore = new PostgisRelationStore(datasource,
          new RelationBuilder(coordinateTransform, geometryFactory, coordinateStore, referenceStore));

      HeaderBlock header = headerMapper.last();
      long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;
      String statePath = statePath(nextSequenceNumber);

      URL stateURL = new URL(String.format("%s/%s", url(source), statePath));
      InputStreamReader reader = new InputStreamReader(new BufferedInputStream(stateURL.openConnection().getInputStream()), Charsets.UTF_8);
      String stateContent = CharStreams.toString(reader);
      State state = State.parse(stateContent);
      String changePath = changePath(nextSequenceNumber);
      URL changeURL = new URL(String.format("%s/%s", url(source), changePath));

      try (InputStream changeInputStream = new GZIPInputStream(new BufferedInputStream(changeURL.openConnection().getInputStream()))) {
        Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
        Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
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


  public String path(long sequenceNumber) {
    String leading = String.format("%09d", sequenceNumber);
    return leading.substring(0, 3) + "/"
        + leading.substring(3, 6) + "/"
        + leading.substring(6, 9);
  }

  public String changePath(long sequenceNumber) {
    return path(sequenceNumber) + ".osc.gz";
  }

  public String statePath(long sequenceNumber) {
    return path(sequenceNumber) + ".state.txt";
  }

}
