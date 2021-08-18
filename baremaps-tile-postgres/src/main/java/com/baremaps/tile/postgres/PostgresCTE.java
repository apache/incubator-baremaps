/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.tile.postgres;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;

class PostgresCTE {

  private final List<SelectItem> selectItems;
  private final FromItem fromItem;
  private final List<Join> joins;

  public PostgresCTE(List<SelectItem> selectItems, FromItem fromItem, List<Join> joins) {
    this.selectItems = selectItems;
    this.fromItem = fromItem;
    this.joins = joins;
  }

  public List<SelectItem> getSelectItems() {
    return selectItems;
  }

  public FromItem getFromItem() {
    return fromItem;
  }

  public List<Join> getJoins() {
    return joins;
  }

  public String getAlias() {
    return String.format("h%x", hashCode()).substring(0, 9);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PostgresCTE)) {
      return false;
    }
    return hashCode() == o.hashCode();
  }

  @Override
  public int hashCode() {
    String selectItemsString = selectItems.toString();
    String fromItemString = fromItem.toString();
    String joinsString =
        Optional.ofNullable(joins).stream()
            .flatMap(List::stream)
            .map(Join::toString)
            .collect(Collectors.joining());
    return Objects.hash(selectItemsString, fromItemString, joinsString);
  }
}
