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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import javax.sql.DataSource;
import org.apache.baremaps.database.database.DatabaseContainerTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresQueryGeneratorTest extends DatabaseContainerTest {

  @Test
  @Tag("integration")
  void generate() {
    DataSource dataSource = dataSource();
    List<PostgresQuery> queries =
        new PostgresQueryGenerator(dataSource, null, "public", null, null, "TABLE").generate();
    assertEquals(3, queries.size());
    assertEquals(
        "SELECT id, hstore(array['version', version::text, 'uid', uid::text, 'timestamp', timestamp::text, 'changeset', changeset::text, 'tags', tags::text, 'lon', lon::text, 'lat', lat::text]), geom FROM osm_nodes",
        queries.get(0).getSql());
  }
}
