package com.baremaps.server;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/")
public class MaputnikResources {

  @GET
  @Path("{path:.*}")
  public Response get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("maputnik/%s", path);
    var bytes = ClassLoader.getSystemClassLoader().getResourceAsStream(path).readAllBytes();
    return Response.ok().entity(bytes).build();
  }

}
