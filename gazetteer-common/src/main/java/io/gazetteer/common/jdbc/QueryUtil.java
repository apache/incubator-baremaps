package io.gazetteer.common.jdbc;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;

public class QueryUtil {

  public static String select(String table, List<String> columns) {
    return String.format(
        "SELECT %s FROM %s",
        Joiner.on(", ").join(columns),
        table);
  }

  public static String select(String table, List<String> columns, String conditions) {
    return String.format(
        "SELECT %s FROM %s WHERE %s",
        Joiner.on(", ").join(columns),
        table,
        conditions);
  }


  public static String insert(String table, List<String> columns) {
    return String.format(
        "INSERT INTO %s (%s) VALUES (%s)",
        table,
        Joiner.on(", ").join(columns),
        Joiner.on(", ").join(columns.stream()
            .map(c -> "?")
            .collect(Collectors.toList())));
  }

  public static String update(String table, List<String> columns, String conditions) {
    return String.format(
        "UPDATE %s SET %s WHERE %s",
        table,
        Joiner.on(", ").join(columns.stream()
            .map(c -> String.format("%s = ?", c))
            .collect(Collectors.toList())),
        conditions);
  }

  public static String delete(String table, String conditions) {
    return String.format(
        "DELETE FROM %s WHERE %s",
        table,
        conditions);
  }

  public static String copyIn(String table, List<String> columns) {
    return String.format(
        "COPY %s (%s) FROM STDIN BINARY",
        table,
        Joiner.on(", ").join(columns));
  }

  public static String where(List<String> columns) {
    return Joiner.on(" AND ").join(columns.stream()
        .map(c -> String.format("%s = ?", c))
        .collect(Collectors.toList()));
  }

}
