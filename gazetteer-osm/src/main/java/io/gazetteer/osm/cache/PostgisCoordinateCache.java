package io.gazetteer.osm.cache;

import io.gazetteer.osm.geometry.GeometryUtil;
import io.gazetteer.osm.store.Store;
import io.gazetteer.osm.store.StoreException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

public class PostgisCoordinateCache implements Store<Long, Coordinate> {

  private static final String SELECT =
      "SELECT st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ANY (?)";

  private final DataSource dataSource;

  public PostgisCoordinateCache(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Coordinate get(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(6));
        return point.getCoordinate();
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Coordinate> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Coordinate> nodes = new HashMap<>();
      while (result.next()) {
        Long id = result.getLong(1);
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(2));
        nodes.put(id, point.getCoordinate());
      }
      return keys.stream().map(key -> nodes.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(Long key, Coordinate values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(List<Entry<Long, Coordinate>> storeEntries) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Long key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void importAll(List<Entry<Long, Coordinate>> values) {
    throw new UnsupportedOperationException();
  }

}
