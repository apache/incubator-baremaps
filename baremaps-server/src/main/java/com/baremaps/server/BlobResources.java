/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    try {
      // normalize and strip asset from invalid inputsd
      URI asset = assets.resolve(assets.resolve(path).normalize().getPath());
      if (!asset.getPath().startsWith(assets.getPath())) {
        throw new IllegalAccessException();
      }
      var bytes = blobStore.get(asset).getInputStream().readAllBytes();
      return Response.ok() // lgtm [java/xss]
          .entity(bytes)
          .build();
    } catch (Exception e) {
      return Response.status(404).build();
    }
  }
}
