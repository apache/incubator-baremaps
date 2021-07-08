package com.baremaps.tile.postgres;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;

class QueryKey {

  private final List<SelectItem> selectItems;
  private final FromItem fromItem;
  private final List<Join> joins;

  public QueryKey(
      List<SelectItem> selectItems,
      FromItem fromItem,
      List<Join> joins) {
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
    return String.format("h%s", Math.abs(hashCode())).substring(0, 9);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QueryKey)) {
      return false;
    }
    return hashCode() == o.hashCode();
  }

  @Override
  public int hashCode() {
    String selectItemsString = selectItems.toString();
    String fromItemString = fromItem.toString();
    String joinsString = Optional.ofNullable(joins).stream()
        .flatMap(List::stream)
        .map(Join::toString)
        .collect(Collectors.joining());
    return Objects.hash(selectItemsString, fromItemString, joinsString);
  }

}
