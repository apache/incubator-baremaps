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

package com.baremaps.openapi.services;

import com.baremaps.api.CollectionsApi;
import com.baremaps.model.Collection;
import com.baremaps.openapi.BaseService;
import java.util.UUID;
import javax.ws.rs.core.Response;

public class CollectionsService extends BaseService implements CollectionsApi {

  @Override
  public Response addCollection(Collection collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response deleteCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response getCollection(UUID collectionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response getCollections() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response updateCollection(UUID collectionId, Collection collection) {
    throw new UnsupportedOperationException();
  }
}
