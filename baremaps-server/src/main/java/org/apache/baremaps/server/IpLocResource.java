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

import com.google.common.net.InetAddresses;
import io.servicetalk.http.api.StreamingHttpRequest;
import io.servicetalk.transport.api.ConnectionContext;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import org.apache.baremaps.iploc.IpLocObject;
import org.apache.baremaps.iploc.IpLocRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that provides access to the IP location database.
 */
@Singleton
@javax.ws.rs.Path("/")
public class IpLocResource {

  private static final Logger logger = LoggerFactory.getLogger(IpLocResource.class);

  private final IpLocRepository ipLocRepository;

  @Inject
  public IpLocResource(IpLocRepository ipLocRepository) {
    this.ipLocRepository = ipLocRepository;
  }

  public record IP(String ip) {
  }

  @GET
  @javax.ws.rs.Path("/api/ip")
  public Response ip(
      @Context ConnectionContext context,
      @Context StreamingHttpRequest request,
      @QueryParam("ip") String ip) {
    try {
      var address = InetAddresses.forString(
          Optional.ofNullable((CharSequence) ip)
              .or(() -> Optional.ofNullable(request.headers().get("X-Forwarded-For")))
              .or(() -> Optional.ofNullable(request.headers().get("X-Real-IP")))
              .orElse(((InetSocketAddress) context.remoteAddress()).getAddress().getHostAddress())
              .toString().split(",")[0].trim());
      return Response.status(200) // lgtm [java/xss]
          .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
          .header(CONTENT_TYPE, APPLICATION_JSON)
          .entity(new IP(address.toString())).build();
    } catch (IllegalArgumentException e) {
      logger.error("Error while processing request", e);
      return Response.serverError().build();
    }
  }

  @GET
  @javax.ws.rs.Path("/api/iploc")
  public Response iploc(
      @Context ConnectionContext context,
      @Context StreamingHttpRequest request,
      @QueryParam("ip") String ip) {
    try {
      var address = InetAddresses.forString(
          Optional.ofNullable((CharSequence) ip)
              .or(() -> Optional.ofNullable(request.headers().get("X-Forwarded-For")))
              .or(() -> Optional.ofNullable(request.headers().get("X-Real-IP")))
              .orElse(((InetSocketAddress) context.remoteAddress()).getAddress().getHostAddress())
              .toString().split(",")[0].trim());
      List<IpLocObject> inetnumLocations = ipLocRepository.findByInetAddress(address);
      List<InetnumLocationDto> inetnumLocationDtos =
          inetnumLocations.stream().map(InetnumLocationDto::new).toList();
      return Response.status(200) // lgtm [java/xss]
          .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
          .header(CONTENT_TYPE, APPLICATION_JSON)
          .entity(inetnumLocationDtos).build();
    } catch (IllegalArgumentException e) {
      logger.error("Error while processing request", e);
      return Response.serverError().build();
    }
  }

  public record InetnumLocationDto(
      String geocoderInput,
      String inetStart,
      String inetEnd,
      double longitude,
      double latitude,
      String network,
      String country,
      String source,
      String precision) {

    public InetnumLocationDto(IpLocObject ipLocObject) {
      this(ipLocObject.geocoderInput(),
          ipLocObject.inetRange().start().toString().substring(1),
          ipLocObject.inetRange().end().toString().substring(1),
          ipLocObject.coordinate().getX(),
          ipLocObject.coordinate().getY(),
          ipLocObject.network(),
          ipLocObject.country(),
          ipLocObject.source(),
          ipLocObject.precision().toString());
    }
  }
}
