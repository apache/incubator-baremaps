package com.baremaps.editor;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class ServerApplication extends Application {

  private final ServerModule module;

  public ServerApplication(ServerModule module) {
    this.module = module;
  }

  @Override
  public Set<Object> getSingletons() {
    return new HashSet<>(asList(module));
  }

  @Override
  public Set<Class<?>> getClasses() {
    return new HashSet<>(asList(
        MultiPartFeature.class,
        ServerService.class
    ));
  }

}
