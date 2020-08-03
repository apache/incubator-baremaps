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

package com.baremaps.osm.store;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.parser.PBFFileBlockHandler;
import java.util.List;
import javax.inject.Inject;

public class StoreImportFileBlockHandler implements PBFFileBlockHandler {

  private final PostgisHeaderStore headerTable;

  private final Store<Node> nodeStore;

  private final Store<Way> wayStore;

  private final Store<Relation> relationStore;

  @Inject
  public StoreImportFileBlockHandler(
      PostgisHeaderStore headerTable,
      Store<Node> nodeStore,
      Store<Way> wayStore,
      Store<Relation> relationStore) {
    this.headerTable = headerTable;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void onHeader(Header header) throws StoreException {
    headerTable.insert(header);
  }

  @Override
  public void onNodes(List<Node> nodes) throws StoreException {
    nodeStore.copy(nodes);
  }

  @Override
  public void onWays(List<Way> ways) throws StoreException {
    wayStore.copy(ways);
  }

  @Override
  public void onRelations(List<Relation> relations) throws StoreException {
    relationStore.copy(relations);
  }

  @Override
  public void onComplete() {

  }

  @Override
  public void onError(Throwable error) {

  }
}
