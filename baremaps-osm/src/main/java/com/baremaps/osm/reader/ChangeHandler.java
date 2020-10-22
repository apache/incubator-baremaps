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
package com.baremaps.osm.reader;

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;

public interface ChangeHandler {

  void onNodeCreate(Node node) throws Exception;

  void onNodeModify(Node node) throws Exception;

  void onNodeDelete(Node node) throws Exception;

  void onWayCreate(Way way) throws Exception;

  void onWayModify(Way way) throws Exception;

  void onWayDelete(Way way) throws Exception;

  void onRelationCreate(Relation relation) throws Exception;

  void onRelationModify(Relation relation) throws Exception;

  void onRelationDelete(Relation relation) throws Exception;

}
