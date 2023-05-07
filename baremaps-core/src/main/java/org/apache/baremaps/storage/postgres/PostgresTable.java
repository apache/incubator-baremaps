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
import org.apache.baremaps.storage.*;
import org.apache.baremaps.utils.GeometryUtils;
import org.locationtech.jts.geom.*;

/**
 * A table that stores rows in a Postgres table.
 */
public class PostgresTable extends AbstractTable {

  private final DataSource dataSource;

  private final Schema schema;

  /**
   * Constructs a table with a given name and a given schema.
   * 
   * @param dataSource the data source
   * @param schema the schema of the table
   */
  public PostgresTable(DataSource dataSource, Schema schema) {
    this.dataSource = dataSource;
    this.schema = schema;
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
  public Stream<Row> stream() {
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
    var countQuery = count(schema);
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
  public Schema schema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(Row row) {
    var query = insert(schema);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      for (int i = 0; i < schema.columns().size(); i++) {
        var value = row.get(schema.columns().get(i).name());
        if (value instanceof Geometry) {
          statement.setBytes(i + 1, GeometryUtils.serialize((Geometry) value));
        } else {
          statement.setObject(i + 1, value);
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
  public boolean addAll(Collection<? extends Row> rows) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(insert(schema))) {
      for (var row : rows) {
        for (int i = 0; i < schema.columns().size(); i++) {
          var value = row.get(schema.columns().get(i).name());
          if (value instanceof Geometry) {
            statement.setBytes(i + 1, GeometryUtils.serialize((Geometry) value));
          } else {
            statement.setObject(i + 1, value);
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
   * @param schema the schema of the table
   * @return the query
   */
  protected static String select(Schema schema) {
    var columns = schema.columns().stream()
        .map(column -> {
          if (column.type().isAssignableFrom(Geometry.class)) {
            return String.format("st_asbinary(\"%s\") AS \"%s\"", column.name(), column.name());
          } else {
            return String.format("\"%s\"", column.name());
          }
        })
        .toList();
    return "SELECT " + String.join(", ", columns) + " FROM \"" + schema.name() + "\"";
  }

  /**
   * Generates a query that counts the number of rows of a table.
   *
   * @param schema the schema of the table
   * @return the query
   */
  protected static String insert(Schema schema) {
    var columns = schema.columns().stream()
        .map(column -> String.format("\"%s\"", column.name()))
        .toList();
    var values = schema.columns().stream()
        .map(column -> "?")
        .toList();
    return "INSERT INTO \""
        + schema.name() + "\" (" + String.join(", ", columns) + ") "
        + "VALUES (" + String.join(", ", values) + ")";
  }

  /**
   * Generates a query that counts the number of rows of a table.
   *
   * @param schema the schema of the table
   * @return the query
   */
  protected String count(Schema schema) {
    return String.format("SELECT COUNT(*) FROM \"%s\"", schema.name());
  }

  /**
   * An iterator that iterates over the rows of a table.
   */
  public class PostgresIterator implements Iterator<Row>, AutoCloseable {

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
        resultSet = statement.executeQuery(select(schema));
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
    public Row next() {
      if (!hasNext) {
        throw new NoSuchElementException();
      }
      try {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < schema.columns().size(); i++) {
          var column = schema.columns().get(i);
          if (column.type().isAssignableFrom(Geometry.class)) {
            values.add(GeometryUtils.deserialize(resultSet.getBytes(i + 1)));
          } else {
            values.add(resultSet.getObject(i + 1));
          }
        }
        hasNext = resultSet.next();
        return new RowImpl(schema, values);
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
