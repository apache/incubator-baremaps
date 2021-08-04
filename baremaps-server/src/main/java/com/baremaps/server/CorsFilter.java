package com.baremaps.server;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

  public static class CorsHeaders
  {
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ORIGIN = "Origin";
    public static final String VARY = "Vary";
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException
  {
    String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
    if (origin == null) {
      return;
    }
    if (requestContext.getMethod().equalsIgnoreCase("OPTIONS"))
    {
      preflight(origin, requestContext);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
  {
    String origin = requestContext.getHeaderString(CorsHeaders.ORIGIN);
    if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS") || requestContext.getProperty("cors.failure") != null)
    {
      return;
    }
    responseContext.getHeaders().putSingle(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    responseContext.getHeaders().putSingle(CorsHeaders.VARY, CorsHeaders.ORIGIN);
  }

  protected void preflight(String origin, ContainerRequestContext requestContext) throws IOException
  {
    // Respond with a 204 no content since we are returning an empty response with just the header for preflight
    Response.ResponseBuilder builder = Response.noContent();
    builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    builder.header(CorsHeaders.VARY, CorsHeaders.ORIGIN);
    builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept, authorization");
    builder.header(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    requestContext.abortWith(builder.build());

  }
}
