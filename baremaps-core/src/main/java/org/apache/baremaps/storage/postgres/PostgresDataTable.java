/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.storage.postgres;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import org.apache.baremaps.collection.store.AbstractDataTable;
import org.apache.baremaps.collection.store.DataRow;
import org.apache.baremaps.collection.store.DataRowImpl;
import org.apache.baremaps.collection.store.DataSchema;
import org.apache.baremaps.utils.GeometryUtils;
import org.locationtech.jts.geom.*;

/**
 * A table that stores rows in a Postgres table.
 */
public class PostgresDataTable extends AbstractDataTable {

  private final DataSource dataSource;

  private final DataSchema dataSchema;

  /**
   * Constructs a table with a given name and a given schema.
   * 
   * @param dataSource the data source
   * @param dataSchema the schema of the table
   */
  public PostgresDataTable(DataSource dataSource, DataSchema dataSchema) {
    this.dataSource = dataSource;
    this.dataSchema = dataSchema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PostgresIterator iterator() {
    return new PostgresIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<DataRow> stream() {
    var iterator = iterator();
    var spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
    var stream = StreamSupport.stream(spliterator, false);
    return stream.onClose(iterator::close);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    var countQuery = count(dataSchema);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(countQuery);
        var resultSet = statement.executeQuery()) {
      resultSet.next();
      return resultSet.getLong(1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return dataSchema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(DataRow dataRow) {
    var query = insert(dataSchema);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      for (int i = 1; i <= dataSchema.columns().size(); i++) {
        var value = dataRow.get(dataSchema.columns().get(i - 1).name());
        if (value instanceof Geometry geometry) {
          statement.setBytes(i, GeometryUtils.serialize(geometry));
        } else {
          statement.setObject(i, value);
        }
      }
      return statement.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(Collection<? extends DataRow> rows) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(insert(dataSchema))) {
      for (var row : rows) {
        for (int i = 1; i <= dataSchema.columns().size(); i++) {
          var value = row.get(dataSchema.columns().get(i - 1).name());
          if (value instanceof Geometry geometry) {
            statement.setBytes(i, GeometryUtils.serialize(geometry));
          } else {
            statement.setObject(i, value);
          }
        }
        statement.addBatch();
      }
      statement.executeBatch();
      return true;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Generates a query that selects all the rows of a table.
   *
   * @param dataSchema the schema of the table
   * @return the query
   */
  protected static String select(DataSchema dataSchema) {
    var columns = dataSchema.columns().stream()
        .map(column -> {
          if (column.type().isAssignableFrom(Geometry.class)) {
            return String.format("st_asbinary(\"%s\") AS \"%s\"", column.name(), column.name());
          } else {
            return String.format("\"%s\"", column.name());
          }
        })
        .toList();
    return "SELECT " + String.join(", ", columns) + " FROM \"" + dataSchema.name() + "\"";
  }

  /**
   * Generates a query that counts the number of rows of a table.
   *
   * @param dataSchema the schema of the table
   * @return the query
   */
  protected static String insert(DataSchema dataSchema) {
    var columns = dataSchema.columns().stream()
        .map(column -> String.format("\"%s\"", column.name()))
        .toList();
    var values = dataSchema.columns().stream()
        .map(column -> "?")
        .toList();
    return "INSERT INTO \""
        + dataSchema.name() + "\" (" + String.join(", ", columns) + ") "
        + "VALUES (" + String.join(", ", values) + ")";
  }

  /**
   * Generates a query that counts the number of rows of a table.
   *
   * @param dataSchema the schema of the table
   * @return the query
   */
  protected String count(DataSchema dataSchema) {
    return String.format("SELECT COUNT(*) FROM \"%s\"", dataSchema.name());
  }

  /**
   * An iterator that iterates over the rows of a table.
   */
  public class PostgresIterator implements Iterator<DataRow>, AutoCloseable {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private boolean hasNext;

    /**
     * Constructs a new postgres iterator.
     */
    public PostgresIterator() {
      try {
        connection = dataSource.getConnection();
        statement = connection.createStatement();
        resultSet = statement.executeQuery(select(dataSchema));
        hasNext = resultSet.next();
      } catch (SQLException e) {
        close();
        throw new RuntimeException("Error while initializing SQL query iterator", e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      if (!hasNext) {
        close();
      }
      return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataRow next() {
      if (!hasNext) {
        throw new NoSuchElementException();
      }
      try {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < dataSchema.columns().size(); i++) {
          var column = dataSchema.columns().get(i);
          if (column.type().isAssignableFrom(Geometry.class)) {
            values.add(GeometryUtils.deserialize(resultSet.getBytes(i + 1)));
          } else {
            values.add(resultSet.getObject(i + 1));
          }
        }
        hasNext = resultSet.next();
        return new DataRowImpl(dataSchema, values);
      } catch (SQLException e) {
        close();
        throw new RuntimeException("Error while fetching the next result", e);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
      try {
        if (resultSet != null) {
          resultSet.close();
        }
        if (statement != null) {
          statement.close();
        }
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Error while closing resources", e);
      }
    }
  }
}
