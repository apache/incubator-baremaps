package com.baremaps.tile.postgres;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

class JSQLParserTest {

  @Test
  void test() throws JSQLParserException {
    assertNotNull(
        CCJSqlParserUtil.parse("SELECT id, hstore(array['tag1', 'tag2'], array[tag1, tag2]), geom FROM table"));
  }

}
