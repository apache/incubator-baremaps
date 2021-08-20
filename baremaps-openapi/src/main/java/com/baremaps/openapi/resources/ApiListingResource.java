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

package com.baremaps.openapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
@Path("")
public class ApiListingResource {

  private final Swagger swagger;

  public ApiListingResource() {
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion(getVersion());
    beanConfig.setSchemes(new String[] {"http"});
    beanConfig.setBasePath("/");
    beanConfig.setResourcePackage("com.baremaps.openapi.services");
    beanConfig.setScan(true);
    this.swagger = beanConfig.getSwagger();
  }

  public String getVersion() {
    try (InputStream input = Resources.getResource("version.txt").openStream()) {
      Properties properties = new Properties();
      properties.load(input);
      return properties.getProperty("version");
    } catch (IOException e) {
      throw new RuntimeException("Unable to read version number");
    }
  }

  @GET
  @Produces({"application/json"})
  @Path("/swagger.json")
  public Response getListingJson(@Context UriInfo uriInfo) {
    return Response.ok(swaggerWithUriInfo(uriInfo)).build();
  }

  @GET
  @Produces({"application/yaml"})
  @Path("/swagger.yaml")
  public Response getListingYaml(@Context UriInfo uriInfo) throws JsonProcessingException {
    return Response.ok(Yaml.mapper().writeValueAsString(swaggerWithUriInfo(uriInfo))).build();
  }

  private Swagger swaggerWithUriInfo(UriInfo uriInfo) {
    Swagger copy = new Swagger();
    copy.setInfo(swagger.getInfo());
    copy.setHost(
        String.format("%s:%s", uriInfo.getBaseUri().getHost(), uriInfo.getBaseUri().getPort()));
    copy.setBasePath(swagger.getBasePath());
    copy.setTags(swagger.getTags());
    copy.setSchemes(swagger.getSchemes());
    copy.setConsumes(swagger.getConsumes());
    copy.setProduces(swagger.getProduces());
    copy.setSecurity(swagger.getSecurity());
    copy.setPaths(swagger.getPaths());
    copy.setSecurityDefinitions(swagger.getSecurityDefinitions());
    copy.setDefinitions(swagger.getDefinitions());
    copy.setParameters(swagger.getParameters());
    copy.setResponses(swagger.getResponses());
    copy.setExternalDocs(swagger.getExternalDocs());
    copy.setVendorExtensions(swagger.getVendorExtensions());
    return copy;
  }
}
