package com.baremaps.openapi.services;

import com.google.common.io.Resources;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/redoc")
public class RedocResource {

  @GET
  public Response get() throws IOException {
    var resource = Resources.getResource("redoc.html");
    var bytes = resource.openStream().readAllBytes();
    return Response.ok().entity(bytes).build();
  }
}
