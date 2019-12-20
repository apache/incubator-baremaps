package io.gazetteer.tiles.util;

import io.gazetteer.osm.stream.BatchSpliterator;
import io.gazetteer.tiles.Tile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public class TileUtil {

  public static final String BBOX = "SELECT st_asewkb(st_transform(st_setsrid(st_extent(geom), 3857), 4326)) as table_extent FROM osm_nodes;";

  public static Geometry bbox(Connection connection) throws SQLException, ParseException {
    try (PreparedStatement statement = connection.prepareStatement(BBOX)) {
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return new WKBReader().read(result.getBytes(1));
      } else {
        return null;
      }
    }
  }

  public static Stream<Tile> getOverlappingXYZ(Geometry geometry, int minZ, int maxZ) {
    Envelope envelope = geometry.getEnvelopeInternal();
    return StreamSupport.stream(new BatchSpliterator<Tile>(IntStream.rangeClosed(minZ, maxZ).mapToObj(z -> z).flatMap(z -> {
      Tile min = getOverlappingXYZ(envelope.getMinX(), envelope.getMaxY(), z);
      Tile max = getOverlappingXYZ(envelope.getMaxX(), envelope.getMinY(), z);
      return IntStream.rangeClosed(min.getX(), max.getX()).mapToObj(i -> i)
          .flatMap(x -> IntStream.rangeClosed(min.getY(), max.getY()).mapToObj(i -> i).map(y -> new Tile(x, y, z)));
    }).spliterator(), 10), true);
  }

  public static Tile getOverlappingXYZ(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 << z));
    return new Tile(x, y, z);
  }

}
