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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

class JSQLParserTest {

  @Test
  void parseArray() throws JSQLParserException {
    Statement statement = CCJSqlParserUtil
        .parse("SELECT id, hstore(array['tag1', 'tag2'], array[tag1, tag2]), geom FROM table");
    assertNotNull(statement);
  }

  @Test
  void parseWithStatement() throws JSQLParserException {
    String sql = "WITH a AS (SELECT c FROM t) SELECT c FROM a";
    Select select = (Select) CCJSqlParserUtil.parse(sql);
    assertNotNull(select);
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

  @Test
  void parseBoolean() throws JSQLParserException {
    String sql = "SELECT true";
    Select select = (Select) CCJSqlParserUtil.parse(sql);
    assertNotNull(select);
  }
}
