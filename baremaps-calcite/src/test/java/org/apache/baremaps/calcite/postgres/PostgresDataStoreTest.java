/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.calcite.postgres;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.baremaps.calcite.DataStoreException;
import org.apache.baremaps.calcite.MockDataTable;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresDataStoreTest extends PostgresContainerTest {

  private PostgresDataStore schema;

  @BeforeEach
  void init() {
    schema = new PostgresDataStore(dataSource());
    schema.add(new MockDataTable());
  }

  @Test
  @Tag("integration")
  void list() {
    var tables = schema.list();
    assertEquals(4, tables.size());
  }

  @Test
  @Tag("integration")
  void addAndGet() {
    var table = schema.get("mock");
    assertNotNull(table);
  }

  @Test
  @Tag("integration")
  void remove() {
    schema.remove("mock");
    assertThrows(DataStoreException.class, () -> schema.get("mock"));
  }
}
