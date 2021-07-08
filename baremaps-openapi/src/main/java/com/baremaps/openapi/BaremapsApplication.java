package com.baremaps.openapi;

import static java.util.Arrays.asList;

import com.baremaps.openapi.services.CollectionsService;
import com.baremaps.openapi.services.ConformanceService;
import com.baremaps.openapi.services.RootService;
import com.baremaps.openapi.services.StylesService;
import com.baremaps.openapi.services.TilesService;
import com.baremaps.openapi.services.TilesetsService;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class BaremapsApplication extends Application {

  private final Binder configuration;

  public BaremapsApplication(Binder configuration) {
    this.configuration = configuration;
  }

  @Override
  public Set<Class<?>> getClasses() {
    return new HashSet<>(asList(
        Feature.class,
        MultiPartFeature.class,
        RootService.class,
        ConformanceService.class,
        CollectionsService.class,
        StylesService.class,
        TilesetsService.class,
        TilesService.class
    ));
  }

  @Override
  public Set<Object> getSingletons() {
    return new HashSet<>(asList(configuration));
  }

}
