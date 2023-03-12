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

package org.apache.baremaps.workflow.tasks;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExecuteSqlTest {

  private static final String INPUT = """
      SELECT 1;
      -- test
      SELECT 2;
      /* test */
      SELECT 3;
      /*
      test
      */
      """;

  private static final String OUTPUT = "SELECT 1;SELECT 2;SELECT 3;";

  @Test
  void removeComments() {
    var queriesWithoutComments = ExecuteSql.removeComments(INPUT).strip();
    assertEquals(OUTPUT.trim(), queriesWithoutComments.replace("\n", "").strip());
  }
}
