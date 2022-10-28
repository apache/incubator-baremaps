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
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Singleton
@Path("/swagger")
public class SwaggerResource {

  public static final String SWAGGER_HTML = "swagger.html";

  @GET
  public Response get() throws IOException {
    try (InputStream inputStream = Resources.getResource(SWAGGER_HTML).openStream()) {
      return Response.ok().entity(inputStream.readAllBytes()).build();
    }
  }
}
