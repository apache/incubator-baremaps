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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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

  public static final List<Object[]> PLAYER_DATA_AS_OBJECT_ARRAY = Arrays.asList(
      new Object[] {1, "Wizard", 5},
      new Object[] {2, "Hunter", 7}

  );
  public static final List<Object[]> EQUIPMENT_DATA_AS_OBJECT_ARRAY = Arrays.asList(
      new Object[] {1, "fireball", 7, 1},
      new Object[] {2, "rifle", 4, 2});


  public static void main(String[] args) throws SQLException {


    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    RelDataTypeFactory.Builder playerType = new RelDataTypeFactory.Builder(typeFactory);

    Properties info = new Properties();
    // https://calcite.apache.org/javadocAggregate/org/apache/calcite/config/Lex.html#JAVA
    info.setProperty("lex", "MYSQL");

    Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
    CalciteConnection calciteConnection =
        connection.unwrap(CalciteConnection.class);

    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    playerType.add("id", SqlTypeName.INTEGER);
    playerType.add("name", SqlTypeName.VARCHAR);
    playerType.add("level", SqlTypeName.INTEGER);

    ListTable playerTable = new ListTable(playerType.build(), PLAYER_DATA_AS_OBJECT_ARRAY);
    rootSchema.add("player", playerTable);

    RelDataTypeFactory.Builder equipmentType = new RelDataTypeFactory.Builder(typeFactory);

    equipmentType.add("id", SqlTypeName.INTEGER);
    equipmentType.add("name", SqlTypeName.VARCHAR);
    equipmentType.add("damage", SqlTypeName.INTEGER);
    equipmentType.add("player_id", SqlTypeName.INTEGER);

    ListTable equipmentTable = new ListTable(equipmentType.build(), EQUIPMENT_DATA_AS_OBJECT_ARRAY);
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
    private final RelDataType rowType;
    private final List<Object[]> data;

    ListTable(RelDataType rowType, List<Object[]> data) {
      this.rowType = rowType;
      this.data = data;
    }

    @Override
    public Enumerable<Object[]> scan(final DataContext root) {
      return Linq4j.asEnumerable(data);
    }

    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
      return rowType;
    }
  }
}
