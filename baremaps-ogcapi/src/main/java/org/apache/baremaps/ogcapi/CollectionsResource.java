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

import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.ogcapi.api.CollectionsApi;
import org.apache.baremaps.ogcapi.model.Collection;
import org.apache.baremaps.ogcapi.model.Collections;
import org.apache.baremaps.ogcapi.model.Link;
import org.apache.baremaps.storage.Table;
import org.apache.baremaps.storage.postgres.PostgresStore;

@Singleton
public class CollectionsResource implements CollectionsApi {

  @Context
  UriInfo uriInfo;

  private final PostgresStore store;

  @Inject
  public CollectionsResource(DataSource dataSource) {
    this.store = new PostgresStore(dataSource);
  }

  @Override
  public Response getCollections() {
    Collections collections = new Collections();
    collections.setTimeStamp(new Date());
    collections.setCollections(store.list().stream()
        .map(store::get)
        .map(this::getCollectionInfo)
        .toList());
    return Response.ok().entity(collections).build();
  }

  @Override
  public Response getCollection(String collectionId) {
    var table = store.get(collectionId);
    var collectionInfo = getCollectionInfo(table);
    return Response.ok().entity(collectionInfo).build();
  }

  private Collection getCollectionInfo(Table table) {
    var name = table.schema().name();
    var collection = new Collection();
    collection.setId(name);
    collection.setTitle(name);
    collection.setDescription(name);
    collection.setLinks(List.of(
        new Link()
            .href(uriInfo.getBaseUriBuilder().path("collections").path(name).build().toString())
            .rel("items")
            .type("application/json")));
    return collection;
  }
}
