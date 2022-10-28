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

import com.fasterxml.jackson.core.util.JacksonFeature;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.apache.baremaps.database.PostgresUtils;
import org.apache.baremaps.model.TileJSON;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.Assert;
import org.junit.Test;

public class TilesetsResourceIntegrationTest extends JerseyTest {

  Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    // Create a data source with a throwaway postgres database
    DataSource dataSource = PostgresUtils.dataSource("jdbc:tc:postgresql:13:///test");
    jdbi = Jdbi.create(dataSource).installPlugin(new Jackson2Plugin());
    jdbi.useHandle(
        handle -> handle.execute("create table tilesets (id uuid primary key, tileset jsonb)"));

    // Configure the service
    return new ResourceConfig().registerClasses(JacksonFeature.class, TilesetsResource.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(dataSource).to(DataSource.class);
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
    TileJSON tileSet = new TileJSON();
    tileSet.setName("test");
    target().path("/tilesets").request(MediaType.APPLICATION_JSON).post(Entity.json(tileSet));

    // List the tilesets
    ids = target().path("/tilesets").request().get(new GenericType<>() {});
    assertEquals(1, ids.size());

    // Get the tileset
    UUID id = ids.get(0);
    tileSet = target().path("/tilesets/" + id).request().get(TileJSON.class);
    Assert.assertEquals("test", tileSet.getName());

    // Delete the tileset
    target().path("/tilesets/" + ids.get(0)).request().delete();

    // List the tilesets
    ids = target().path("/tilesets").request().get(new GenericType<>() {});
    assertEquals(0, ids.size());
  }
}
