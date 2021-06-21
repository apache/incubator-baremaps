package com.baremaps.openapi.services;

import com.baremaps.api.DefaultApi;
import com.baremaps.model.LandingPage;
import com.baremaps.model.Link;

public class RootService implements DefaultApi {

  @Override
  public LandingPage getLandingPage() {

    LandingPage landingPage = new LandingPage();

    landingPage.setTitle("Baremaps");
    landingPage.setDescription("Baremaps OGC API Landing Page");

    String address = "localhost:8080"; // TODO: Get this from server context

    Link linkRoot = new Link();
    linkRoot.title("This document (landing page)");
    linkRoot.setHref(String.format("http://%s/", address));
    linkRoot.setRel("application/json");
    landingPage.getLinks().add(linkRoot);

    Link linkConformance = new Link();
    linkConformance.title("Conformance declaration");
    linkConformance.setHref(String.format("http://%s/conformance", address));
    linkConformance.setRel("application/json");
    landingPage.getLinks().add(linkConformance);

    return landingPage;

  }
}
