/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.database.calcite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.calcite.runtime.AccumOperation;
import org.apache.calcite.runtime.CollectOperation;
import org.apache.calcite.runtime.SpatialTypeFunctions;
import org.apache.calcite.runtime.UnionOperation;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.sql.fun.SqlSpatialTypeFunctions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

public class CalciteTest {


  @Test
  public void test() throws SQLException {
    GeometryFactory geometryFactory = new GeometryFactory();

    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Add the spatial functions to the root schema
      ImmutableList<String> emptyPath = ImmutableList.of();
      ModelHandler.addFunctions(rootSchema, null, emptyPath,
          SpatialTypeFunctions.class.getName(), "*", true);
      ModelHandler.addFunctions(rootSchema, null, emptyPath,
          SqlSpatialTypeFunctions.class.getName(), "*", true);

      rootSchema.add("ST_UNION", AggregateFunctionImpl.create(UnionOperation.class));
      rootSchema.add("ST_ACCUM", AggregateFunctionImpl.create(AccumOperation.class));
      rootSchema.add("ST_COLLECT", AggregateFunctionImpl.create(CollectOperation.class));

      ModelHandler.addFunctions(rootSchema, "ST_AsMVTGeom", emptyPath,
          VectorTileFunctions.class.getName(), "asVectorTileGeom", true);
      ModelHandler.addFunctions(rootSchema, "ST_AsMVT", emptyPath,
          VectorTileFunctions.class.getName(), "asVectorTile", true);

      // Create the city table
      DataRowType cityRowType = new DataRowTypeImpl("city", List.of(
          new DataColumnImpl("id", Type.INTEGER),
          new DataColumnImpl("name", Type.STRING),
          new DataColumnImpl("geometry", Type.GEOMETRY)));
      DataTable cityDataTable = new DataTableImpl(
          cityRowType,
          new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(cityRowType))));
      cityDataTable.add(new DataRowImpl(cityDataTable.rowType(),
          List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
      cityDataTable.add(new DataRowImpl(cityDataTable.rowType(),
          List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));
      SqlDataTable citySqlDataTable = new SqlDataTable(cityDataTable);
      rootSchema.add("city", citySqlDataTable);

      // Create the population table
      DataRowType populationRowType = new DataRowTypeImpl("population", List.of(
          new DataColumnImpl("city_id", Type.INTEGER),
          new DataColumnImpl("population", Type.INTEGER)));
      DataTable populationDataTable = new DataTableImpl(
          populationRowType,
          new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(populationRowType))));
      populationDataTable
          .add(new DataRowImpl(populationDataTable.rowType(), List.of(1, 2_161_000)));
      populationDataTable
          .add(new DataRowImpl(populationDataTable.rowType(), List.of(2, 8_336_000)));
      SqlDataTable populationSqlDataTable = new SqlDataTable(populationDataTable);
      rootSchema.add("population", populationSqlDataTable);

      // Query the database
      String sql = """
          SELECT ST_AsText(ST_AsMVTGeom(
          	ST_GeomFromText('POLYGON ((0 0, 10 1, 10 10, 1 10, 0 0))'),
          	ST_MakeEnvelope(0, 0, 4096, 4096),
          	4096, 0, true))
          	""";

      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {
        assertTrue(resultSet.next());
        assertEquals("POLYGON ((0 4096, 10 4095, 10 4086, 1 4086, 0 4096))",
            resultSet.getString(1));
      }
    }
  }
}
