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



import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * A resources that provides the OpenAPI specification and the Swagger UI.
 */
@Singleton
@Path("/")
public class ApiResource {

  @Context
  UriInfo uriInfo;

  private static final String SWAGGER = "swagger.html";

  private static final String MAP = "map.html";

  private static final String OPENAPI = "ogcapi.yaml";

  /**
   * Constructs an {@code ApiResource}.
   */
  public ApiResource() {}

  /**
   * Returns the Swagger UI.
   *
   * @return the Swagger UI
   * @throws IOException if an I/O error occurs
   */
  @GET
  @Path("/swagger")
  @Produces({"text/html"})
  public Response getSwaggerUI() throws IOException {
    try (InputStream inputStream = Resources.getResource(SWAGGER).openStream()) {
      return Response.ok().entity(inputStream.readAllBytes()).build();
    }
  }

  /**
   * Returns the map UI.
   *
   * @return the map UI
   * @throws IOException if an I/O error occurs
   */
  @GET
  @Path("/map")
  @Produces({"text/html"})
  public Response getMap() throws IOException {
    try (InputStream inputStream = Resources.getResource(MAP).openStream()) {
      return Response.ok(inputStream.readAllBytes()).build();
    }
  }

  /**
   * Returns the OpenAPI specification in JSON format.
   *
   * @return the OpenAPI specification
   * @throws IOException if an I/O error occurs
   */
  @GET
  @Path("/api")
  @Produces({"application/json"})
  public Response getJsonSpecification() throws IOException {
    try (InputStream inputStream = Resources.getResource(OPENAPI).openStream()) {
      var openAPI = new OpenAPIV3Parser()
          .readContents(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
          .getOpenAPI();
      openAPI.setServers(List.of(new Server().url(
          String.format(
              "%s://%s:%s/",
              uriInfo.getBaseUri().getScheme(),
              uriInfo.getBaseUri().getHost(),
              uriInfo.getBaseUri().getPort()))));
      var mapper = Json.mapper();
      mapper.registerModule(new JavaTimeModule());
      var json = mapper.writeValueAsString(openAPI);
      return Response.ok(json).build();
    }
  }
}
