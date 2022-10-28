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



import com.google.common.io.Resources;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
@Path("")
public class ApiResource {

  private static String openapiPath;

  public ApiResource(String openapiPath) {
    ApiResource.openapiPath = openapiPath;
  }

  public String getVersion() {
    try (InputStream input = Resources.getResource("version.txt").openStream()) {
      Properties properties = new Properties();
      properties.load(input);
      return properties.getProperty("version");
    } catch (IOException e) {
      throw new RuntimeException("Unable to read version number");
    }
  }

  @GET
  @Produces({"application/json"})
  @Path("/api")
  public Response getListingJson(@Context UriInfo uriInfo) throws IOException {
    return Response.ok(Json.mapper().writeValueAsString(parseOpenapi(uriInfo))).build();
  }

  @GET
  @Produces({"application/yaml"})
  @Path("/api")
  public Response getListingYaml(@Context UriInfo uriInfo) throws IOException {
    return Response.ok(Yaml.mapper().writeValueAsString(parseOpenapi(uriInfo))).build();
  }

  private OpenAPI parseOpenapi(UriInfo uriInfo) throws IOException {
    try (InputStream inputStream = Resources.getResource(openapiPath).openStream()) {
      var openAPI = new OpenAPIV3Parser()
          .readContents(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
          .getOpenAPI();
      openAPI.setServers(List.of(new Server().url(
          String.format("%s:%s", uriInfo.getBaseUri().getHost(), uriInfo.getBaseUri().getPort()))));
      return openAPI;
    }
  }
}
