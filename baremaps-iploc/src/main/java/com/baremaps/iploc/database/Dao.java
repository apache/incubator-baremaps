/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.iploc.database;

import java.util.List;
import java.util.Optional;

/**
 * A basic DAO interface to implement the data access object pattern
 *
 * @param <T> the type of entity
 */
public interface Dao<T> {

  Optional<T> findOne(long id);

  List<T> findAll();

  void save(T t);

  void update(T t, String[] params);

  void delete(T t);
}
