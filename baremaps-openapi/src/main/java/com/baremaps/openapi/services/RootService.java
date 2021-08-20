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

import com.baremaps.api.DefaultApi;
import com.baremaps.model.LandingPage;
import com.baremaps.model.Link;
import com.baremaps.openapi.BaseService;
import java.net.URI;
import javax.ws.rs.core.Response;

public class RootService extends BaseService implements DefaultApi {

  @Override
  public Response getLandingPage() {

    LandingPage landingPage = new LandingPage();

    landingPage.setTitle("Baremaps");
    landingPage.setDescription("Baremaps OGC API Landing Page");
    URI address = this.getBaseUri();

    Link linkRoot = new Link();
    linkRoot.title("This document (landing page)");
    linkRoot.setHref(address.toString());
    linkRoot.setRel("application/json");
    landingPage.getLinks().add(linkRoot);

    Link linkConformance = new Link();
    linkConformance.title("Conformance declaration");
    linkConformance.setHref(address.toString());
    linkConformance.setRel("application/json");
    landingPage.getLinks().add(linkConformance);

    return Response.ok().entity(landingPage).build();
  }
}
