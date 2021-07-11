package com.baremaps.server;

import com.baremaps.blob.BlobStore;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@javax.ws.rs.Path("/")
public class BlobResources {

  private final BlobStore blobStore;

  private final URI assets;

  @Inject
  public BlobResources(BlobStore blobStore, @Named("assets") URI assets) {
    this.blobStore = blobStore;
    this.assets = assets;
  }

  @GET
  @javax.ws.rs.Path("{path:.*}")
  public Response get(@PathParam("path") String path) {
    if (path.equals("") || path.endsWith("/")){
      path += "index.html";
    }
    try {
      // normalize and strip asset from invalid inputsd
      URI asset = assets.resolve(assets.resolve(path).normalize().getPath());
      if (!asset.getPath().startsWith(assets.getPath())) {
        throw new IllegalAccessException();
      }
      var bytes = blobStore.readByteArray(asset);
      return Response.ok() // lgtm [java/xss]
          .entity(bytes)
          .build();
    } catch (Exception e) {
      return Response.status(404).build();
    }
  }

}
