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



import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Singleton
@Path("")
public class StudioResource {

  private static final QualifiedType<ObjectNode> ENTITY =
      QualifiedType.of(ObjectNode.class).with(Json.class);

  private final Jdbi jdbi;

  @Inject
  public StudioResource(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("studio/{kind}")
  public Response getEntities(@PathParam("kind") String kind) {
    List<ObjectNode> entityList = jdbi.withHandle(
        handle -> handle.createQuery("select entity from studio.entities where kind = :kind")
            .bind("kind", kind).mapTo(ENTITY).list());
    return Response.ok(entityList).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("studio/{kind}")
  public Response addEntity(ObjectNode entity, @PathParam("kind") String kind) {
    UUID id;
    try {
      id = UUID.fromString(entity.get("id").asText());
    } catch (Exception e) {
      id = UUID.randomUUID();
      entity.put("id", id.toString());
    }

    UUID finalId = id;
    jdbi.useHandle(handle -> handle.createUpdate(
        "insert into studio.entities (id, entity, kind) values (:id, CAST(:entity AS jsonb), :kind)")
        .bind("id", finalId).bindByType("entity", entity, ENTITY).bind("kind", kind).execute());
    return Response.created(URI.create("studio/" + kind + "/" + id)).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("studio/{kind}/{id}")
  public Response getEntity(@PathParam("id") UUID id, @PathParam("kind") String kind) {
    ObjectNode entity = jdbi.withHandle(handle -> handle
        .createQuery("select entity from studio.entities where id = :id and kind = :kind")
        .bind("id", id).bind("kind", kind).mapTo(ENTITY).one());
    return Response.ok(entity).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("studio/{kind}/{id}")
  public Response updateEntity(ObjectNode entity, @PathParam("id") UUID id,
      @PathParam("kind") String kind) {
    jdbi.useHandle(handle -> handle.createUpdate(
        "update studio.entities set map = CAST(:entity AS jsonb) where id = :id and kind = :kind")
        .bind("id", id).bindByType("entity", entity, ENTITY).bind("kind", kind).execute());
    return Response.noContent().build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("studio/{kind}/{id}")
  public Response deleteEntity(@PathParam("id") UUID id, @PathParam("kind") String kind) {
    jdbi.useHandle(handle -> handle
        .execute("delete from studio.entities where id = (?) and kind = (?)", id, kind));
    return Response.noContent().build();
  }
}
