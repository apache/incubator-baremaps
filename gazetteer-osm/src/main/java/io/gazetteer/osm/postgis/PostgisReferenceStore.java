package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Entry;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.StoreException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PostgisReferenceStore implements Store<Long, List<Long>> {

  private static final String SELECT =
      "SELECT nodes FROM osm_ways WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT nodes FROM osm_ways WHERE id IN (?)";

  private final DataSource dataSource;

  public PostgisReferenceStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<Long> get(Long id) {
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(1);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        return nodes;
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<List<Long>> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setArray(1, connection.createArrayOf("bigint", keys.toArray()));
      ResultSet result = statement.executeQuery();
      List<List<Long>> references = new ArrayList<>();
      while (result.next()) {
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(1);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        references.add(nodes);
      }
      return references;
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(Long key, List<Long> values) {
    throw new NotImplementedException();
  }

  @Override
  public void putAll(List<Entry<Long, List<Long>>> entries) {
    throw new NotImplementedException();
  }

  @Override
  public void delete(Long key) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new NotImplementedException();
  }

  @Override
  public void importAll(List<Entry<Long, List<Long>>> values) {
    throw new NotImplementedException();
  }
}
