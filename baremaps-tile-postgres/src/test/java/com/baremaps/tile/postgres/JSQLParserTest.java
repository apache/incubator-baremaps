package com.baremaps.tile.postgres;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

public class JSQLParserTest {

  @Test
  public void test() throws JSQLParserException {
    CCJSqlParserUtil.parse("SELECT id, hstore(array['tag1', 'tag2'], array[tag1, tag2]), geom FROM table");
  }

}
