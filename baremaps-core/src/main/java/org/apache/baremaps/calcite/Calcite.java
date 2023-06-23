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

package org.apache.baremaps.calcite;

import com.google.common.collect.ImmutableList;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.baremaps.collection.AppendOnlyBuffer;
import org.apache.baremaps.collection.DataCollectionAdapter;
import org.apache.baremaps.collection.IndexedDataList;
import org.apache.baremaps.collection.store.*;
import org.apache.baremaps.collection.type.RowDataType;
import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.runtime.SpatialTypeFunctions;
import org.apache.calcite.runtime.SpatialTypeFunctions.Accum;
import org.apache.calcite.runtime.SpatialTypeFunctions.Collect;
import org.apache.calcite.runtime.SpatialTypeFunctions.Union;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.sql.fun.SqlSpatialTypeFunctions;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.*;

public class Calcite {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static final DataSchema CITY_SCHEMA = new DataSchemaImpl("country", List.of(
      new DataColumnImpl("id", Integer.class),
      new DataColumnImpl("name", String.class),
      new DataColumnImpl("geometry", Geometry.class)));

  private static final DataTable CITY_TABLE = new DataTableImpl(
      CITY_SCHEMA,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(CITY_SCHEMA))));

  static {
    CITY_TABLE.add(new DataRowImpl(CITY_TABLE.schema(),
        List.of(1, "Paris", GEOMETRY_FACTORY.createPoint(new Coordinate(2.3522, 48.8566)))));
    CITY_TABLE.add(new DataRowImpl(CITY_TABLE.schema(),
        List.of(2, "New York", GEOMETRY_FACTORY.createPoint(new Coordinate(-74.0060, 40.7128)))));
  }

  private static final DataSchema POPULATION_SCHEMA = new DataSchemaImpl("population", List.of(
      new DataColumnImpl("country_id", Integer.class),
      new DataColumnImpl("population", Integer.class)));

  private static final DataTable POPULATION_TABLE = new DataTableImpl(
      POPULATION_SCHEMA,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(POPULATION_SCHEMA))));

  static {
    POPULATION_TABLE
        .add(new DataRowImpl(POPULATION_TABLE.schema(), List.of(1, 2_161_000)));
    POPULATION_TABLE
        .add(new DataRowImpl(POPULATION_TABLE.schema(), List.of(2, 8_336_000)));
  }

  public static void main(String[] args) throws SQLException {
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      final ImmutableList<String> emptyPath = ImmutableList.of();
      ModelHandler.addFunctions(rootSchema, null, emptyPath,
          SpatialTypeFunctions.class.getName(), "*", true);
      ModelHandler.addFunctions(rootSchema, null, emptyPath,
          SqlSpatialTypeFunctions.class.getName(), "*", true);

      rootSchema.add("ST_UNION", AggregateFunctionImpl.create(Union.class));
      rootSchema.add("ST_ACCUM", AggregateFunctionImpl.create(Accum.class));
      rootSchema.add("ST_COLLECT", AggregateFunctionImpl.create(Collect.class));

      ListTable cityTable = new ListTable(CITY_TABLE);
      rootSchema.add("country", cityTable);

      ListTable populationTable = new ListTable(POPULATION_TABLE);
      rootSchema.add("population", populationTable);

      String sql = """
          SELECT name, ST_Buffer(geometry, 10), population
          FROM country
          INNER JOIN population
          ON country.id = population.country_id""";

      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          System.out.println(resultSet.getString(1)
              + ", " + resultSet.getObject(2)
              + ", " + resultSet.getInt(3));
        }
      }
    }

  }

  /**
   * A simple table based on a list.
   */
  private static class ListTable extends AbstractTable implements ScannableTable {

    private final DataTable table;

    ListTable(DataTable table) {
      this.table = table;
    }

    @Override
    public Enumerable<Object[]> scan(final DataContext root) {
      Collection<Object[]> collection =
          new DataCollectionAdapter<>(table, row -> row.values().toArray());
      return Linq4j.asEnumerable(collection);
    }

    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
      var rowType = new RelDataTypeFactory.Builder(typeFactory);
      for (DataColumn column : table.schema().columns()) {
        rowType.add(column.name(), toSqlType(column.type()));
      }
      return rowType.build();
    }

    private RelDataType toSqlType(Class type) {
      if (type.equals(String.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.VARCHAR);
      } else if (type.equals(Boolean.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.BOOLEAN);
      } else if (type.equals(Short.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.SMALLINT);
      } else if (type.equals(Integer.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.INTEGER);
      } else if (type.equals(Float.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.FLOAT);
      } else if (type.equals(Double.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.DOUBLE);
      } else if (type.equals(Geometry.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(Point.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(LineString.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(Polygon.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(MultiPoint.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(MultiLineString.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(MultiPolygon.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else if (type.equals(GeometryCollection.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.GEOMETRY);
      } else {
        throw new IllegalArgumentException("Unsupported type " + type);
      }
    }
  }
}
