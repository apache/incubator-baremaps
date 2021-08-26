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

package com.baremaps.openapi.resources;

import com.baremaps.api.DefaultApi;
import com.baremaps.model.LandingPage;
import com.baremaps.model.Link;
import java.util.Arrays;
import javax.ws.rs.core.Response;

public class RootService implements DefaultApi {

  @Override
  public Response getLandingPage() {
    String address = "localhost:8080";

    LandingPage landingPage =
        new LandingPage()
            .title("Baremaps")
            .description("Baremaps OGC API Landing Page")
            .links(
                Arrays.asList(
                    new Link()
                        .title("This document (landing page)")
                        .href(String.format("http://%s/", address))
                        .type("application/json")
                        .rel("self"),
                    new Link()
                        .title("Conformance declaration")
                        .href(String.format("http://%s/conformance", address))
                        .type("application/json")
                        .rel("conformance"),
                    new Link()
                        .title("API description")
                        .href(String.format("http://%s/api", address))
                        .type("application/json")
                        .rel("service-desc"),
                    new Link()
                        .title("API description")
                        .href(String.format("http://%s/api", address))
                        .type("application/yaml")
                        .rel("service-desc"),
                    new Link()
                        .title("API documentation")
                        .href(String.format("http://%s/redoc", address))
                        .type("text/html")
                        .rel("service-doc"),
                    new Link()
                        .title("API documentation")
                        .href(String.format("http://%s/swagger", address))
                        .type("text/html")
                        .rel("service-doc")));
    return Response.ok().entity(landingPage).build();
  }
}
