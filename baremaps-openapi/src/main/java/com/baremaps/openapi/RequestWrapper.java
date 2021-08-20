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

package com.baremaps.openapi;

import java.net.URI;
import javax.ws.rs.core.*;

public class RequestWrapper {
  @Context HttpHeaders requestHeaders;
  @Context UriInfo uriInfo;

  public HttpHeaders getRequestHeaders() {
    return requestHeaders;
  }

  public URI getBaseUri() {
    UriBuilder baseUri = UriBuilder.fromUri(uriInfo.getBaseUri());

    MultivaluedMap headers = requestHeaders.getRequestHeaders();

    if (headers.get("X-Forwarded-Proto") != null) {
      baseUri.scheme(headers.getFirst("X-Forwarded-Proto").toString());
    }
    if (headers.get("X-Forwarded-Host") != null) {
      baseUri.host(headers.getFirst("X-Forwarded-Host").toString());
    }
    return baseUri.build();
  }
}
