/*
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

package org.apache.baremaps.server;



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
  private final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
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
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) {
    String origin = requestContext.getHeaderString(ORIGIN);
    if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS")
        || requestContext.getProperty("cors.failure") != null) {
      return;
    }
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_METHODS,
        "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_HEADERS,
        "origin, content-type, accept, authorization");
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    responseContext.getHeaders().putSingle(ACCESS_CONTROL_EXPOSE_HEADERS, "Location");
    responseContext.getHeaders().putSingle(VARY, ORIGIN);
  }

  protected void preflight(String origin, ContainerRequestContext requestContext) {
    // Respond with a 204 no content since we are returning an empty response with just the header
    // for preflight
    Response.ResponseBuilder builder = Response.noContent();
    builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    builder.header(VARY, ORIGIN);
    builder.header(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    builder.header(ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept, authorization");
    builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    builder.header(ACCESS_CONTROL_EXPOSE_HEADERS, "Location");
    requestContext.abortWith(builder.build());
  }
}
