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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;

public class Calcite {

  private static final DataSchema PLAYER_DATA_SCHEMA = new DataSchemaImpl("player", List.of(
      new DataColumnImpl("id", Integer.class),
      new DataColumnImpl("name", String.class),
      new DataColumnImpl("level", Integer.class)));

  private static final DataTable PLAYER_DATA_TABLE = new DataTableImpl(
      PLAYER_DATA_SCHEMA,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(PLAYER_DATA_SCHEMA))));

  static {
    PLAYER_DATA_TABLE.add(new DataRowImpl(PLAYER_DATA_TABLE.schema(), List.of(1, "Wizard", 5)));
    PLAYER_DATA_TABLE.add(new DataRowImpl(PLAYER_DATA_TABLE.schema(), List.of(2, "Hunter", 7)));
  }

  private static final DataSchema EQUIPMENT_DATA_SCHEMA = new DataSchemaImpl("equipment", List.of(
      new DataColumnImpl("id", Integer.class),
      new DataColumnImpl("name", String.class),
      new DataColumnImpl("damage", Integer.class),
      new DataColumnImpl("player_id", Integer.class)));

  private static final DataTable EQUIPMENT_DATA_TABLE = new DataTableImpl(
      EQUIPMENT_DATA_SCHEMA,
      new IndexedDataList<>(new AppendOnlyBuffer<>(new RowDataType(EQUIPMENT_DATA_SCHEMA))));

  static {
    EQUIPMENT_DATA_TABLE
        .add(new DataRowImpl(EQUIPMENT_DATA_TABLE.schema(), List.of(1, "fireball", 7, 1)));
    EQUIPMENT_DATA_TABLE
        .add(new DataRowImpl(EQUIPMENT_DATA_TABLE.schema(), List.of(2, "rifle", 4, 2)));
  }

  public static void main(String[] args) throws SQLException {
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");

    Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
    CalciteConnection calciteConnection =
        connection.unwrap(CalciteConnection.class);

    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    ListTable playerTable = new ListTable(PLAYER_DATA_TABLE);
    rootSchema.add("player", playerTable);

    ListTable equipmentTable = new ListTable(EQUIPMENT_DATA_TABLE);
    rootSchema.add("equipment", equipmentTable);

    String sql =
        "SELECT player.name, equipment.name FROM player INNER JOIN equipment ON player.id = equipment.player_id ";
    ResultSet resultSet = connection.createStatement().executeQuery(sql);
    StringBuilder b = new StringBuilder();
    while (resultSet.next()) {
      b.append(resultSet.getString(1)).append(" attacks with ");
      b.append(resultSet.getString(2)).append(" !\n");
    }
    System.out.println(b);

    resultSet.close();
  }

  /**
   * A simple table based on a list.
   */
  private static class ListTable extends AbstractTable implements ScannableTable {

    private final DataTable dataTable;

    ListTable(DataTable dataTable) {
      this.dataTable = dataTable;
    }

    @Override
    public Enumerable<Object[]> scan(final DataContext root) {
      Collection<Object[]> collection =
          new DataCollectionAdapter<>(dataTable, row -> row.values().toArray());
      return Linq4j.asEnumerable(collection);
    }

    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
      var rowType = new RelDataTypeFactory.Builder(typeFactory);
      for (DataColumn dataColumn : dataTable.schema().columns()) {
        rowType.add(dataColumn.name(), toSqlType(dataColumn.type()));
      }
      return rowType.build();
    }

    private RelDataType toSqlType(Class type) {
      if (type.equals(Integer.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.INTEGER);
      } else if (type.equals(String.class)) {
        return new JavaTypeFactoryImpl().createSqlType(SqlTypeName.VARCHAR);
      } else {
        throw new IllegalArgumentException();
      }
    }
  }
}
