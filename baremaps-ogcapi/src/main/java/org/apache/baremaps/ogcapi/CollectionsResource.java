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



import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.api.CollectionsApi;
import org.apache.baremaps.model.Collection;
import org.apache.baremaps.model.Collections;
import org.apache.baremaps.model.Link;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Singleton
public class CollectionsResource implements CollectionsApi {

  @Context
  UriInfo uriInfo;

  private static final QualifiedType<Collection> COLLECTION =
      QualifiedType.of(Collection.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public CollectionsResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Response addCollection(Collection collection) {
    collection.setId(UUID.randomUUID());
    jdbi.useHandle(handle -> handle
        .createUpdate("insert into collections (id, collection) values (:id, :collection)")
        .bind("id", collection.getId()).bindByType("collection", collection, COLLECTION).execute());
    return Response.created(URI.create("collections/" + collection.getId())).build();
  }

  @Override
  public Response deleteCollection(UUID collectionId) {
    jdbi.useHandle(handle -> handle.execute(
        String.format("drop table if exists \"%s\"; delete from collections where id = (?)",
            collectionId),
        collectionId));
    return Response.noContent().build();
  }

  @Override
  public Response getCollection(UUID collectionId) {
    Collection collection = jdbi.withHandle(
        handle -> handle.createQuery("select collection from collections where id = :id")
            .bind("id", collectionId).mapTo(COLLECTION).one());
    collection.getLinks().add(new Link().href(uriInfo.getRequestUri().toString()).rel("self"));
    return Response.ok(collection).build();
  }

  @Override
  public Response getCollections() {
    List<Collection> collectionList = jdbi.withHandle(handle -> handle
        .createQuery("select collection from collections").mapTo(COLLECTION).list());
    collectionList.forEach(collection -> collection.getLinks().add(new Link()
        .href(uriInfo.getRequestUri().toString() + "/" + collection.getId()).rel("self")));
    Collections collections = new Collections().collections(collectionList);
    collections.getLinks().add(new Link().href(uriInfo.getRequestUri().toString()).rel("self"));
    return Response.ok(collections).build();
  }

  @Override
  public Response updateCollection(UUID collectionId, Collection collection) {
    jdbi.useHandle(handle -> handle
        .createUpdate("update collections set collection = :collection where id = :id")
        .bind("id", collectionId).bindByType("collection", collection, COLLECTION).execute());
    return Response.noContent().build();
  }
}
