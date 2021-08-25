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

import static org.junit.Assert.assertEquals;

import com.baremaps.model.LandingPage;
import com.baremaps.openapi.resources.RootService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class RootServiceTest extends JerseyTest {

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return new ResourceConfig(RootService.class);
  }

  @Test
  public void testRoot() {
    LandingPage landingPage = target().path("").request().get(LandingPage.class);
    assertEquals("Baremaps", landingPage.getTitle());
  }
}
