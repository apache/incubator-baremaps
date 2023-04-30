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

package org.apache.baremaps.ogcapi;


import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.ogcapi.api.CollectionsApi;
import org.apache.baremaps.ogcapi.model.AllCollections;
import org.apache.baremaps.ogcapi.model.CollectionInfo;
import org.apache.baremaps.ogcapi.model.Collections;
import org.apache.baremaps.storage.postgres.PostgresStore;
import org.jdbi.v3.core.Jdbi;

@Singleton
public class CollectionsResource implements CollectionsApi {

  @Context
  UriInfo uriInfo;

  private final DataSource dataSource;

  @Inject
  public CollectionsResource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Response getCollectionsList(String datetime, List<Double> bbox, Integer limit, String f) {
    var store = new PostgresStore(dataSource);
    var collectionInfoList = store.list().stream().map(name -> {
      var collection = new CollectionInfo();
      collection.setId(name);
      return collection;
    }).toList();
    Collections collections = new Collections();
    collections.setCollections(collectionInfoList);
    return Response.ok().entity(collections).build();
  }

  @Override
  public Response getCollection(AllCollections collectionId, String f) {
    collectionId.name()
    throw new UnsupportedOperationException();
  }
}
