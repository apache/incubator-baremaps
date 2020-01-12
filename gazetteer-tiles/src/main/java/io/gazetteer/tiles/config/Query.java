package io.gazetteer.tiles.config;

public class Query {

  private String sql;

  public Query() {

  }

  public Query(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }
}
