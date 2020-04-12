/*
 * Copyright (C) 2011 The Baremaps Authors
 *
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

package com.baremaps.osm.database;

import java.util.List;

public interface Table<Row> {

  Row select(Long id);

  List<Row> select(List<Long> ids);

  void insert(Row row);

  void insert(List<Row> rows);

  void delete(Long id);

  void delete(List<Long> ids);

  void copy(List<Row> rows);

}
