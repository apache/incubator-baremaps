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


import static org.apache.baremaps.storage.postgres.PostgresStore.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.baremaps.database.metadata.TableMetadata;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.apache.baremaps.storage.*;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresTable extends AbstractTable {

  private static final Logger logger = LoggerFactory.getLogger(PostgresStore.class);

  private final DataSource dataSource;

  private final TableMetadata tableMetadata;

  private final Schema schema;

  public PostgresTable(DataSource dataSource, TableMetadata tableMetadata) {
    this.dataSource = dataSource;
    this.tableMetadata = tableMetadata;
    this.schema = getSchema(tableMetadata);
  }

  @Override
  public Iterator<Row> iterator() {
    try {
      var connection = dataSource.getConnection();
      var statement = connection.prepareStatement(select(schema));
      var resultSet = statement.executeQuery();
      var stream = Stream.generate(() -> {
        try {
          boolean next = resultSet.next();
          if (next) {
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < schema.columns().size(); i++) {
              var column = schema.columns().get(i);
              if (column.type().isAssignableFrom(Geometry.class)) {
                values.add(GeometryUtils.deserialize(resultSet.getBytes(i + 1)));
              } else {
                values.add(resultSet.getObject(i + 1));
              }
            }
            return (Row) new RowImpl(schema, values);
          } else {
            resultSet.close();
            statement.close();
            connection.close();
            return null;
          }
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }).takeWhile(Objects::nonNull);
      return stream.iterator();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

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

  @Override
  public Schema schema() {
    return schema;
  }

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

  protected static Schema getSchema(TableMetadata tableMetadata) {
    var name = tableMetadata.table().tableName();
    var columns = tableMetadata.columns().stream()
        .map(column -> new ColumnImpl(column.columnName(), nameToType.get(column.typeName())))
        .map(Column.class::cast)
        .toList();
    return new SchemaImpl(name, columns);
  }

  protected static String select(Schema schema) {
    var columns = schema.columns().stream()
        .map(column -> {
          if (column.type().isAssignableFrom(Geometry.class)) {
            return String.format("st_asbinary(\"%s\") AS %s", column.name(), column.name());
          } else {
            return String.format("\"%s\"", column.name());
          }
        })
        .toList();
    return "SELECT " + String.join(", ", columns) + " FROM \"" + schema.name() + "\"";
  }

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

  protected String count(Schema schema) {
    return String.format("SELECT COUNT(*) FROM %s", schema.name());
  }
}
