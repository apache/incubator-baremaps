package com.baremaps.openapi;

import static java.util.Arrays.asList;

import com.baremaps.openapi.services.CollectionsService;
import com.baremaps.openapi.services.RootService;
import com.baremaps.openapi.services.StylesService;
import com.baremaps.openapi.services.TilesService;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class BaremapsApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return new HashSet<>(asList(
        Feature.class,
        MultiPartFeature.class,
        RootService.class,
        StylesService.class,
        CollectionsService.class,
        TilesService.class
    ));
  }
}
