package com.baremaps.openapi;

import com.baremaps.openapi.services.RootService;
import com.baremaps.openapi.services.TilesService;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class BaremapsApplication extends Application<BaremapsConfiguration> {

  @Override
  public String getName() {
    return "hello-world";
  }

  @Override
  public void initialize(Bootstrap<BaremapsConfiguration> bootstrap) {
    // nothing to do yet
  }

  @Override
  public void run(BaremapsConfiguration configuration, Environment environment) {
    environment.jersey().register(new RootService());
    environment.jersey().register(new TilesService());
  }

  public static void main(String[] args) throws Exception {
    new BaremapsApplication().run(args);
  }

}
