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

import static com.google.common.net.HttpHeaders.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;
import org.apache.baremaps.iploc.data.InetnumLocation;
import org.apache.baremaps.iploc.data.Ipv4;
import org.apache.baremaps.iploc.database.InetnumLocationDao;
import org.apache.baremaps.iploc.dto.InetnumLocationDto;

@Singleton
@javax.ws.rs.Path("/")
public class IplocResources {

  private final InetnumLocationDao inetnumLocationDao;

  @Inject
  public IplocResources(InetnumLocationDao inetnumLocationDao) {
    this.inetnumLocationDao = inetnumLocationDao;
  }

  @GET
  @javax.ws.rs.Path("/api/ip/{ip}")
  public Response getIpToLocation(@PathParam("ip") String ip) {
    try {
      Ipv4 ipv4 = new Ipv4(ip); // TODO : If the IP is ipv6, it will not throw an error on IPV4 but
                                // contain the wrong
                                // bytes...
      List<InetnumLocation> inetnumLocations = inetnumLocationDao.findByIp(ipv4.getIp());
      List<InetnumLocationDto> inetnumLocationDtos =
          inetnumLocations.stream().map(InetnumLocationDto::new).toList();
      return Response.status(200) // lgtm [java/xss]
          .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").header(CONTENT_TYPE, APPLICATION_JSON)
          .entity(inetnumLocationDtos).build();
    } catch (IllegalArgumentException e) {
      return Response.status(400).entity(e.getMessage()).build();
    }
  }

  @GET
  @javax.ws.rs.Path("/{path:.*}")
  public Response get(@PathParam("path") String path) {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("iploc/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (NullPointerException | IOException e) {
      return Response.status(404).build();
    }
  }
}
