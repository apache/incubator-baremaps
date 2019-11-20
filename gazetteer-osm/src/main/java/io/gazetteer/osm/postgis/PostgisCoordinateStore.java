package io.gazetteer.osm.postgis;

import static io.gazetteer.common.postgis.GeometryUtils.toGeometry;

import io.gazetteer.osm.model.Entry;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.StoreException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PostgisCoordinateStore implements Store<Long, Coordinate> {

  private static final String SELECT =
      "SELECT st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id IN (?)";

  private static final String COPY =
      "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  public PostgisCoordinateStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Coordinate get(Long id) {
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        Point point = (Point) toGeometry(result.getBytes(6));
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
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("bigint", keys.toArray()));
      ResultSet result = statement.executeQuery();
      List<Coordinate> nodes = new ArrayList<>();
      while (result.next()) {
        Point point = (Point) toGeometry(result.getBytes(7));
        nodes.add(point.getCoordinate());
      }
      return nodes;
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Long key, Coordinate value) {
    throw new NotImplementedException();
  }

  @Override
  public void putAll(List<Entry<Long, Coordinate>> entries) {
    throw new NotImplementedException();
  }

  public void delete(Long id) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new NotImplementedException();
  }

  public void importAll(List<Entry<Long, Coordinate>> entries) {
    throw new NotImplementedException();
  }

}
