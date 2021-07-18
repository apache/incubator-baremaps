package com.baremaps.server;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext)
      throws IOException {
    containerResponseContext.getHeaders().add(
        "Access-Control-Allow-Origin",
        "*");
    containerResponseContext.getHeaders().add(
        "Access-Control-Allow-Credentials",
        "true");
    containerResponseContext.getHeaders().add(
        "Access-Control-Allow-Headers",
        "origin, content-type, accept, authorization");
    containerResponseContext.getHeaders().add(
        "Access-Control-Allow-Methods",
        "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}
