package com.baremaps.tile.postgres;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

class JSQLParserTest {

  @Test
  void parseArray() throws JSQLParserException {
    Statement statement = CCJSqlParserUtil.parse("SELECT id, hstore(array['tag1', 'tag2'], array[tag1, tag2]), geom FROM table");
    assertNotNull(statement);
  }

  @Test
  void parseWithStatement() throws JSQLParserException {
    String sql = "WITH a AS (SELECT c FROM t) SELECT c FROM a";
    Select select = (Select) CCJSqlParserUtil.parse(sql);
    assertNotNull(select);
    System.out.println(select.getWithItemsList());
    System.out.println(select.getWithItemsList().get(0).getItemsList());
    System.out.println(select.getWithItemsList().get(0).getName());
    System.out.println(select.getWithItemsList().get(0).getSubSelect());
    System.out.println(select.getWithItemsList().get(0).getWithItemList());
    System.out.println(select.getSelectBody());
  }

  @Test
  void parseUnionStatement() throws JSQLParserException {
    String sql = "SELECT a FROM t1 UNION ALL SELECT a FROM t2";
    Select select = (Select) CCJSqlParserUtil.parse(sql);
    assertNotNull(select);
  }

  @Test
  void parseVariable() throws JSQLParserException {
    String sql = "SELECT $variable FROM table";
    Select select = (Select) CCJSqlParserUtil.parse(sql);
    assertNotNull(select);
  }

}
