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

package org.apache.baremaps.storage.postgres;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.baremaps.storage.MockTable;
import org.apache.baremaps.storage.TableException;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresStoreTest extends PostgresContainerTest {

  private PostgresStore store;

  @BeforeEach
  void init() {
    store = new PostgresStore(dataSource());
    store.add(new MockTable());
  }

  @Test
  @Tag("integration")
  void list() {
    var tables = store.list();
    assertEquals(7, tables.size());
  }

  @Test
  @Tag("integration")
  void addAndGet() {
    var table = store.get("mock");
    assertNotNull(table);
  }

  @Test
  @Tag("integration")
  void remove() {
    store.remove("mock");
    assertThrows(TableException.class, () -> store.get("mock"));
  }
}
