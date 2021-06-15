package com.baremaps.openapi;

import com.baremaps.openapi.services.*;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class BaremapsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>(asList(
                Feature.class,
                MultiPartFeature.class,
                RootService.class,
                ConformanceService.class,
                CollectionsService.class,
                StylesService.class,
                TilesService.class
        ));
    }
}
