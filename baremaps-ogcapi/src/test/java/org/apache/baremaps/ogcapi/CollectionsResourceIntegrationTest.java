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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.baremaps.model.Collection;
import org.apache.baremaps.model.Collections;
import org.apache.baremaps.model.Link;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Assert;
import org.junit.Test;

public class CollectionsResourceIntegrationTest extends JerseyTest {

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
    jdbi.useHandle(handle -> handle
        .execute("create table collections (id uuid primary key, collection jsonb)"));

    // Configure the service
    return new ResourceConfig().register(CollectionsResource.class).register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(jdbi).to(Jdbi.class);
      }
    });
  }

  @Test
  public void test() {
    // Create a new collection
    Collection collection =
        new Collection().title("test").links(List.of(new Link().href("/link").rel("self")));

    // List the collections
    Collections collections = target().path("/collections").request().get(Collections.class);
    Assert.assertEquals(0, collections.getCollections().size());

    // Insert the collection
    Response response = target().path("/collections").request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(collection, MediaType.valueOf("application/json")));
    assertEquals(201, response.getStatus());

    // List the collections
    collections = target().path("/collections").request().get(Collections.class);
    Assert.assertEquals(1, collections.getCollections().size());

    // Get the collection
    String id = response.getHeaderString("Location").split("/")[4];
    collection = target().path("/collections/" + id).request().get(Collection.class);
    Assert.assertEquals("test", collection.getTitle());

    // Update the collection
    collection.setTitle("test_update");
    response = target().path("/collections/" + id).request()
        .put(Entity.entity(collection, MediaType.valueOf("application/json")));
    assertEquals(204, response.getStatus());

    // Delete the collection
    response = target().path("/collections/" + id).request().delete();
    assertEquals(204, response.getStatus());

    // List the collections
    collections = target().path("/collections").request().get(Collections.class);
    Assert.assertEquals(0, collections.getCollections().size());
  }
}
