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

import com.baremaps.model.TileSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Test;

public class TilesetsServiceTest extends JerseyTest {

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
    jdbi.useHandle(
        handle -> handle.execute("create table tilesets (id uuid primary key, tileset jsonb)"));

    // Configure the service
    return new ResourceConfig()
        .register(TilesetsService.class)
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
    // List the tilesets
    List<UUID> ids = target().path("/tilesets").request().get(new GenericType<>() {});
    assertEquals(0, ids.size());

    // Create a new tileset with the service
    TileSet tileSet = new TileSet();
    tileSet.setName("test");
    target().path("/tilesets").request(MediaType.APPLICATION_JSON).post(Entity.json(tileSet));

    // List the tilesets
    ids = target().path("/tilesets").request().get(new GenericType<>() {});
    assertEquals(1, ids.size());

    // Get the tileset
    UUID id = ids.get(0);
    tileSet = target().path("/tilesets/" + id).request().get(TileSet.class);
    assertEquals("test", tileSet.getName());

    // Delete the tileset
    target().path("/tilesets/" + ids.get(0)).request().delete();

    // List the tilesets
    ids = target().path("/tilesets").request().get(new GenericType<>() {});
    assertEquals(0, ids.size());
  }
}
