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

package com.baremaps.studio.resources;

import static org.junit.Assert.assertEquals;

import com.baremaps.model.MapMetadata;
import com.baremaps.model.MapsMetadata;
import com.baremaps.openapi.resources.StudioResource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Test;

public class StudioResourceTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a connection to a throwaway postgres database
    Connection connection;
    try {
      connection = DriverManager.getConnection("jdbc:tc:postgresql:13:///test");
    } catch (SQLException throwables) {
      throw new RuntimeException("Unable to connect to the database");
    }

    // Initialize the database
    jdbi = Jdbi.create(connection).installPlugin(new Jackson2Plugin());
    jdbi.useHandle(handle -> handle.execute("create table maps (id uuid primary key, map jsonb)"));

    // Configure the service
    return new ResourceConfig()
        .register(StudioResource.class)
        .register(
            new AbstractBinder() {
              @Override
              protected void configure() {
                bind(jdbi).to(Jdbi.class);
              }
            });
  }

  @Test
  public void test() {
    // List the maps
    MapsMetadata maps = target().path("/maps").request().get(MapsMetadata.class);
    assertEquals(0, maps.getMapsMetadata().size());

    // Create a new map with the service
    MapMetadata map = new MapMetadata().title("My Map");
    Response response =
        target()
            .path("/maps")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(map, MediaType.valueOf("application/json")));
    assertEquals(201, response.getStatus());

    // List the maps
    maps = target().path("/maps").request().get(MapsMetadata.class);
    assertEquals(1, maps.getMapsMetadata().size());

    // Get the map
    String[] paths = response.getHeaderString("Location").split("/");
    String id = paths[paths.length - 1];
    map = target().path("/maps/" + id).request().get(MapMetadata.class);
    assertEquals("My Map", map.getTitle());

    // Delete the map
    response = target().path("/maps/" + id).request().delete();
    assertEquals(204, response.getStatus());

    // List the maps
    maps = target().path("/maps").request().get(MapsMetadata.class);
    assertEquals(0, maps.getMapsMetadata().size());
  }
}
