/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.ogcapi;



import java.util.Arrays;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.ogcapi.api.DefaultApi;
import org.apache.baremaps.ogcapi.model.LandingPage;
import org.apache.baremaps.ogcapi.model.Link;

/**
 * Root resource (exposed at "/" path).
 */
@Singleton
public class DefaultResource implements DefaultApi {

  @Context
  UriInfo uriInfo;

  /**
   * Get the landing page.
   * 
   * @return the landing page
   */
  @Override
  public Response getLandingPage() {
    var baseURI = uriInfo.getBaseUri().toString();
    var landingPage =
        new LandingPage().title("Baremaps").description("Baremaps OGC API Landing Page")
            .links(Arrays.asList(
                new Link()
                    .title("This document (landing page)")
                    .href(baseURI)
                    .type("application/json")
                    .rel("self"),
                new Link()
                    .title("Conformance declaration")
                    .href(baseURI + "conformance")
                    .type("application/json")
                    .rel("conformance"),
                new Link()
                    .title("API description")
                    .href(baseURI + "api")
                    .type("application/json")
                    .rel("service-desc"),
                new Link()
                    .title("API description")
                    .href(baseURI + "api")
                    .type("application/yaml")
                    .rel("service-desc"),
                new Link()
                    .title("API documentation")
                    .href(baseURI + "swagger")
                    .type("text/html")
                    .rel("service-doc")));
    return Response.ok(landingPage).build();
  }
}
