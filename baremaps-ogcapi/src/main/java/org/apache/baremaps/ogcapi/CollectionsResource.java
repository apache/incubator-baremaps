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
import org.apache.baremaps.collection.store.DataTable;
import org.apache.baremaps.ogcapi.api.CollectionsApi;
import org.apache.baremaps.ogcapi.model.Collection;
import org.apache.baremaps.ogcapi.model.Collections;
import org.apache.baremaps.ogcapi.model.Link;
import org.apache.baremaps.storage.postgres.PostgresDataStore;

/**
 * A resource that provides access to collections.
 */
@Singleton
public class CollectionsResource implements CollectionsApi {

  @Context
  UriInfo uriInfo;

  private final PostgresDataStore store;

  /**
   * Constructs a {@code CollectionsResource}.
   *
   * @param dataSource the datasource
   */
  @Inject
  public CollectionsResource(DataSource dataSource) {
    this.store = new PostgresDataStore(dataSource);
  }

  /**
   * Returns the collections.
   *
   * @return the collections
   */
  @Override
  public Response getCollections() {
    Collections collections = new Collections();
    collections.setTimeStamp(new Date());
    collections.setCollections(store.list().stream()
        .map(store::get)
        .map(this::getCollection)
        .toList());
    return Response.ok().entity(collections).build();
  }

  /**
   * Returns the collection with the specified id.
   *
   * @param collectionId the collection id
   * @return the collection
   */
  @Override
  public Response getCollection(String collectionId) {
    var table = store.get(collectionId);
    var collectionInfo = getCollection(table);
    return Response.ok().entity(collectionInfo).build();
  }

  /**
   * Returns the collection info for the specified table.
   *
   * @param dataTable the table
   * @return the collection info
   */
  private Collection getCollection(DataTable dataTable) {
    var name = dataTable.schema().name();
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
