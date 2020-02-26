package io.gazetteer.cli.commands;


import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.gazetteer.core.io.InputStreams;
import io.gazetteer.core.postgis.PostgisHelper;
import io.gazetteer.osm.cache.PostgisCoordinateCache;
import io.gazetteer.osm.cache.PostgisReferenceCache;
import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.geometry.RelationBuilder;
import io.gazetteer.osm.geometry.WayBuilder;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmxml.Change;
import io.gazetteer.osm.osmxml.ChangeConsumer;
import io.gazetteer.osm.osmxml.ChangeSpliterator;
import io.gazetteer.osm.osmxml.State;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import io.gazetteer.osm.store.Store;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update")
public class Update implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

  @Parameters(
      index = "0",
      paramLabel = "REPLICATION_DIRECTORY",
      description = "The replication directory.")
  private String source;

  @Parameters(
      index = "1",
      paramLabel = "POSTGRES_DATABASE",
      description = "The postgres database.")
  private String database;

  @Override
  public Integer call() throws Exception {
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(epsg4326, epsg3857);
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
    Store<Long, Coordinate> coordinateStore = new PostgisCoordinateCache(datasource);
    Store<Long, List<Long>> referenceStore = new PostgisReferenceCache(datasource);
    PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
    PostgisNodeStore nodeStore = new PostgisNodeStore(datasource,
        new NodeBuilder(coordinateTransform, geometryFactory));
    PostgisWayStore wayStore = new PostgisWayStore(datasource,
        new WayBuilder(coordinateTransform, geometryFactory, coordinateStore));
    PostgisRelationStore relationStore = new PostgisRelationStore(datasource,
        new RelationBuilder(coordinateTransform, geometryFactory, coordinateStore, referenceStore));

    HeaderBlock header = headerMapper.getLast();
    long nextSequenceNumber = header.getReplicationSequenceNumber() + 1;
    String statePath = statePath(nextSequenceNumber);

    String stateURL = String.format("%s/%s", source, statePath);
    InputStreamReader reader = new InputStreamReader(InputStreams.from(stateURL), Charsets.UTF_8);
    String stateContent = CharStreams.toString(reader);
    State state = State.parse(stateContent);
    String changePath = changePath(nextSequenceNumber);
    String changeURL = String.format("%s/%s", source, changePath);

    try (InputStream changeInputStream = new GZIPInputStream(InputStreams.from(changeURL))) {
      Spliterator<Change> spliterator = new ChangeSpliterator(changeInputStream);
      Stream<Change> changeStream = StreamSupport.stream(spliterator, true);
      ChangeConsumer changeConsumer = new ChangeConsumer(nodeStore, wayStore, relationStore);
      changeStream.forEach(changeConsumer);
    }

    headerMapper.insert(new HeaderBlock(
        state.timestamp,
        state.sequenceNumber,
        header.getReplicationUrl(),
        header.getSource(),
        header.getWritingProgram(),
        header.getBbox()));

    return 0;

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
