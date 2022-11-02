/*
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

package org.apache.baremaps.ogcapi;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class StudioResourceIntegrationTest extends JerseyTest {

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
    jdbi.useHandle(handle -> handle.execute("create schema studio"));
    jdbi.useHandle(handle -> handle
        .execute("create table studio.entities (id uuid primary key, entity jsonb, kind text)"));

    // Configure the service
    return new ResourceConfig().register(StudioResource.class).register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(jdbi).to(Jdbi.class);
      }
    });
  }

  @Test
  public void test() {
    // List the maps
    ArrayNode entities = target().path("studio/maps").request().get(ArrayNode.class);
    assertEquals(0, entities.size());

    // Create a new map with the service
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode entity = mapper.createObjectNode().put("title", "My Map").put("views", 3);
    Response response = target().path("studio/maps").request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(entity, MediaType.valueOf("application/json")));
    assertEquals(201, response.getStatus());

    // List the maps
    entities = target().path("studio/maps").request().get(ArrayNode.class);
    assertEquals(1, entities.size());

    // Get the map
    String[] paths = response.getHeaderString("Location").split("/");
    String id = paths[paths.length - 1];
    entity = target().path("studio/maps/" + id).request().get(ObjectNode.class);
    assertEquals("My Map", entity.get("title").textValue());

    // Delete the map
    response = target().path("studio/maps/" + id).request().delete();
    assertEquals(204, response.getStatus());

    // List the maps
    entities = target().path("studio/maps").request().get(ArrayNode.class);
    assertEquals(0, entities.size());
  }
}
