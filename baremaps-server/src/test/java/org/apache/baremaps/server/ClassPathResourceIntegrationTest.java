/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.server;

import static org.junit.Assert.assertTrue;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

public class ClassPathResourceIntegrationTest extends JerseyTest {

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    return new ResourceConfig()
        .register(ClassPathResource.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind("assets").to(String.class).named("directory");
            bind("viewer.html").to(String.class).named("index");
          }
        });
  }

  @Test
  public void testAssetsDirectory() {
    assertTrue(target().path("").request().get(String.class).contains("<title>Baremaps</title>"));
    assertTrue(target().path("viewer.html").request().get(String.class)
        .contains("<title>Baremaps</title>"));
    assertTrue(target().path("server.html").request().get(String.class)
        .contains("<title>Baremaps</title>"));
  }
}
