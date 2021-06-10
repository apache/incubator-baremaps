package com.baremaps.editor;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/")
public class StaticResources {

  @GET
  @Path("{path:.*}")
  public Response Get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("maputnik/%s", path);
    var bytes = ClassLoader.getSystemClassLoader().getResourceAsStream(path).readAllBytes();
    return Response.ok().entity(bytes).build();
  }

}
