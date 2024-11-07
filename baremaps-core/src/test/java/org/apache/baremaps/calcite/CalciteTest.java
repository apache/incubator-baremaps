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

package org.apache.baremaps.calcite;


import com.google.common.collect.ImmutableList;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.apache.baremaps.data.calcite.SqlDataTable;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.baremaps.data.store.BaremapsDataTable;
import org.apache.baremaps.data.type.RowDataType;
import org.apache.baremaps.maplibre.vectortile.VectorTileFunctions;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.Type;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.runtime.AccumOperation;
import org.apache.calcite.runtime.CollectOperation;
import org.apache.calcite.runtime.SpatialTypeFunctions;
import org.apache.calcite.runtime.UnionOperation;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.sql.fun.SqlSpatialTypeFunctions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

class CalciteTest {

  @Test
  void test() throws SQLException {
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
      DataSchema cityRowType = new DataSchemaImpl("city", List.of(
          new DataColumnFixed("id", Cardinality.OPTIONAL, Type.INTEGER),
          new DataColumnFixed("name", Cardinality.OPTIONAL, Type.STRING),
          new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY)));
      DataTable cityDataTable = new BaremapsDataTable(
          cityRowType,
          new IndexedDataList<>(new AppendOnlyLog<>(new RowDataType(cityRowType))));
      cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
          List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
      cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
          List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));
      SqlDataTable citySqlDataTable = new SqlDataTable(cityDataTable);
      rootSchema.add("city", citySqlDataTable);

      // Create the population table
      DataSchema populationRowType = new DataSchemaImpl("population", List.of(
          new DataColumnFixed("city_id", Cardinality.OPTIONAL, Type.INTEGER),
          new DataColumnFixed("population", Cardinality.OPTIONAL, Type.INTEGER)));
      DataTable populationDataTable = new BaremapsDataTable(
          populationRowType,
          new IndexedDataList<>(new AppendOnlyLog<>(new RowDataType(populationRowType))));
      populationDataTable
          .add(new DataRowImpl(populationDataTable.schema(), List.of(1, 2_161_000)));
      populationDataTable
          .add(new DataRowImpl(populationDataTable.schema(), List.of(2, 8_336_000)));
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
        Assertions.assertTrue(resultSet.next());
        Assertions.assertEquals("POLYGON ((0 4096, 10 4095, 10 4086, 1 4086, 0 4096))",
            resultSet.getString(1));
      }
    }
  }
}
