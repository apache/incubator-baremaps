/*
 * Code inspired by resteasy CorsFilter: https://github.com/resteasy/Resteasy/blob/22984e96a2f40dc556ba7b1613422febd94f86d2/resteasy-core/src/main/java/org/jboss/resteasy/plugins/interceptors/CorsFilter.java
 *
 Copyright (c) Resteasy Maintainers. All rights reserved.
 Licensed under the ASL license.
 */

package com.baremaps.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  private final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  private final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
  private final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
  private final String ORIGIN = "Origin";
  private final String VARY = "Vary";

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String origin = requestContext.getHeaderString(ORIGIN);
    if (origin == null) {
      return;
    }
    if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
      preflight(origin, requestContext);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    String origin = requestContext.getHeaderString(ORIGIN);
    if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS")
        || requestContext.getProperty("cors.failure") != null) {
      return;
    }
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept, authorization");
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    responseContext.getHeaders().putSingle(VARY, ORIGIN);
  }

  protected void preflight(String origin, ContainerRequestContext requestContext) {
    // Respond with a 204 no content since we are returning an empty response with just the header for preflight
    Response.ResponseBuilder builder = Response.noContent();
    builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    builder.header(VARY, ORIGIN);
    builder.header(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    builder.header(ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept, authorization");
    builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    requestContext.abortWith(builder.build());
  }
}
