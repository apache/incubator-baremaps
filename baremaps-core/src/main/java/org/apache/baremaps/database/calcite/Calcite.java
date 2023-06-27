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

package org.apache.baremaps.database.calcite;

import com.google.common.collect.ImmutableList;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.apache.baremaps.database.collection.AppendOnlyBuffer;
import org.apache.baremaps.database.collection.IndexedDataList;
import org.apache.baremaps.database.schema.*;
import org.apache.baremaps.database.schema.DataColumn.Type;
import org.apache.baremaps.database.type.RowDataType;
import org.apache.baremaps.vectortile.VectorTileFunctions;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.runtime.SpatialTypeFunctions;
import org.apache.calcite.runtime.SpatialTypeFunctions.Accum;
import org.apache.calcite.runtime.SpatialTypeFunctions.Collect;
import org.apache.calcite.runtime.SpatialTypeFunctions.Union;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.sql.fun.SqlSpatialTypeFunctions;
import org.locationtech.jts.geom.*;

public class Calcite {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static final DataRowType CITY_ROW_TYPE = new DataRowTypeImpl("city", List.of(
      new DataColumnImpl("id", Type.INTEGER),
      new DataColumnImpl("name", Type.STRING),
      new DataColumnImpl("geometry", Type.GEOMETRY)));

  private static final DataTable CITY_TABLE = new DataTableImpl(
      CITY_ROW_TYPE,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(CITY_ROW_TYPE))));

  static {
    CITY_TABLE.add(new DataRowImpl(CITY_TABLE.rowType(),
        List.of(1, "Paris", GEOMETRY_FACTORY.createPoint(new Coordinate(2.3522, 48.8566)))));
    CITY_TABLE.add(new DataRowImpl(CITY_TABLE.rowType(),
        List.of(2, "New York", GEOMETRY_FACTORY.createPoint(new Coordinate(-74.0060, 40.7128)))));
  }

  private static final DataRowType POPULATION_ROW_TYPE = new DataRowTypeImpl("population", List.of(
      new DataColumnImpl("city_id", Type.INTEGER),
      new DataColumnImpl("population", Type.INTEGER)));

  private static final DataTable POPULATION_TABLE = new DataTableImpl(
      POPULATION_ROW_TYPE,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(POPULATION_ROW_TYPE))));

  static {
    POPULATION_TABLE
        .add(new DataRowImpl(POPULATION_TABLE.rowType(), List.of(1, 2_161_000)));
    POPULATION_TABLE
        .add(new DataRowImpl(POPULATION_TABLE.rowType(), List.of(2, 8_336_000)));
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

      ModelHandler.addFunctions(rootSchema, "ST_AsMVTGeom", emptyPath, VectorTileFunctions.class.getName(), "asVectorTileGeom", true);
      ModelHandler.addFunctions(rootSchema, "ST_AsMVT", emptyPath, VectorTileFunctions.class.getName(), "asVectorTile", true);

      SqlDataTable cityTable = new SqlDataTable(CITY_TABLE);
      rootSchema.add("city", cityTable);
      SqlDataTable populationTable = new SqlDataTable(POPULATION_TABLE);
      rootSchema.add("population", populationTable);

      String sql = """
         SELECT ST_AsText(ST_AsMVTGeom(
         	ST_GeomFromText('POLYGON ((0 0, 10 1, 10 10, 1 10, 0 0))'),
         	ST_MakeEnvelope(0, 0, 4096, 4096),
         	4096, 0, true))
         	""";

      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          System.out.println(resultSet.getString(1));
        }
      }
    }
  }

}
