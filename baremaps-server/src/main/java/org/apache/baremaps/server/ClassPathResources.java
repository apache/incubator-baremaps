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

package org.apache.baremaps.server;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Serves static resources from the class path.
 */
@Singleton
@javax.ws.rs.Path("/")
public class ClassPathResources {

  private final String directory;

  private final String index;

  /**
   * Constructs a {@code ClassPathResources}.
   *
   * @param directory the directory
   * @param index the index
   */
  @Inject
  public ClassPathResources(
      @Named("directory") String directory,
      @Named("index") String index) {
    this.directory = directory;
    this.index = index;
  }

  /**
   * Serves a static resource from the class path.
   *
   * @param path the path
   * @return the response
   */
  @GET
  @javax.ws.rs.Path("{path:.*}")
  public Response get(@PathParam("path") String path) {
    if (path.equals("") || path.endsWith("/")) {
      path += index;
    }
    path = String.format("%s/%s", directory, path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      return Response.ok().entity(inputStream.readAllBytes()).build();
    } catch (NullPointerException | IOException e) {
      return Response.status(404).build();
    }
  }
}
