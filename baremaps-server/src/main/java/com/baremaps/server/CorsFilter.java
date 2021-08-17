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

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(
      ContainerRequestContext containerRequestContext,
      ContainerResponseContext containerResponseContext)
      throws IOException {
    containerResponseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
    containerResponseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    containerResponseContext
        .getHeaders()
        .add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
    containerResponseContext
        .getHeaders()
        .add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}
