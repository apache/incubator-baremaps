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

package org.apache.baremaps.database.tile;



import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Models the groups identified in the input queries of a {@code PostgresTileStore}. These groups
 * are used to form common table expressions (CTE).
 */
class PostgresGroup {

  private final List<SelectItem> selectItems;
  private final FromItem fromItem;
  private final List<Join> joins;

  /**
   * Constructs a {@code PostgresGroup} with objects extracted from an AST obtained by parsing a SQL
   * query with JSQLParser.
   *
   * @param selectItems the selected columns.
   * @param fromItem the from clause
   * @param joins the join clauses
   */
  public PostgresGroup(List<SelectItem> selectItems, FromItem fromItem, List<Join> joins) {
    this.selectItems = selectItems;
    this.fromItem = fromItem;
    this.joins = joins;
  }

  /**
   * Returns the selected columns.
   *
   * @return the selected columns
   */
  public List<SelectItem> getSelectItems() {
    return selectItems;
  }

  /**
   * Returns the from clause.
   *
   * @return the from clause
   */
  public FromItem getFromItem() {
    return fromItem;
  }

  /**
   * Returns the join clauses.
   *
   * @return the join clauses
   */
  public List<Join> getJoins() {
    return joins;
  }

  /**
   * Returns the unique alias of this group.
   *
   * @return the alias
   */
  public String getAlias() {
    return String.format("h%x", hashCode());
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PostgresGroup)) {
      return false;
    }
    return hashCode() == o.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    String selectItemsString = selectItems.toString();
    String fromItemString = fromItem.toString();
    String joinsString = Optional.ofNullable(joins).stream().flatMap(List::stream)
        .map(Join::toString).collect(Collectors.joining());
    return Objects.hash(selectItemsString, fromItemString, joinsString);
  }
}
