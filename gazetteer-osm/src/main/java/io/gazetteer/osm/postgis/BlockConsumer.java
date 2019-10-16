package io.gazetteer.osm.postgis;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.data.FixedSizeObjectMap;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.postgresql.PGConnection;

public class BlockConsumer extends FileBlockConsumer {

  private final DataSource pool;

  private final FixedSizeObjectMap<Coordinate> coordinateMap;

  public BlockConsumer(DataSource pool, FixedSizeObjectMap<Coordinate> coordinateMap) {
    checkNotNull(pool);
    this.pool = pool;
    this.coordinateMap = coordinateMap;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try (Connection connection = pool.getConnection()) {
      HeaderTable.insert(connection, headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      NodeTable.copy(pgConnection, primitiveBlock.getDenseNodes());
      WayTable.copy(pgConnection, primitiveBlock.getWays());
      primitiveBlock.getWays().stream().forEach(way -> {
        List<Coordinate> nodes = way.getNodes().stream().map(node -> coordinateMap.get(node)).collect(Collectors.toList());
        System.out.println(nodes);
      });
      RelationTable.copy(pgConnection, primitiveBlock.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
