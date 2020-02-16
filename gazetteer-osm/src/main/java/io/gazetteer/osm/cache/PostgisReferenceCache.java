package io.gazetteer.osm.cache;

import io.gazetteer.osm.store.Store;
import io.gazetteer.osm.store.StoreException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class PostgisReferenceCache implements Store<Long, List<Long>> {

  private static final String SELECT =
      "SELECT nodes FROM osm_ways WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, nodes FROM osm_ways WHERE id WHERE id = ANY (?)";

  private final DataSource dataSource;

  public PostgisReferenceCache(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<Long> get(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
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
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, List<Long>> references = new HashMap<>();
      while (result.next()) {
        List<Long> nodes = new ArrayList<>();
        long id = result.getLong(1);
        Array array = result.getArray(2);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        references.put(id, nodes);
      }
      return keys.stream().map(key -> references.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(Long key, List<Long> values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(List<Entry<Long, List<Long>>> storeEntries) {
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
  public void importAll(List<Entry<Long, List<Long>>> values) {
    throw new UnsupportedOperationException();
  }

}
