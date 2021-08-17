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
package com.baremaps.openapi;

import static java.util.Arrays.asList;

import com.baremaps.openapi.services.CollectionsService;
import com.baremaps.openapi.services.ConformanceService;
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
    return new HashSet<>(
        asList(
            Feature.class,
            MultiPartFeature.class,
            RootService.class,
            ConformanceService.class,
            CollectionsService.class,
            StylesService.class,
            TilesService.class));
  }
}
