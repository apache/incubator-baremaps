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

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.newContextResolver;
import static org.apache.baremaps.config.DefaultObjectMapper.defaultObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.apache.baremaps.database.PostgresUtils;
import org.apache.baremaps.database.tile.PostgresTileStore;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.mvt.tileset.Tileset;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Tag;

@Tag("integration")
public abstract class OgcApiTest extends JerseyTest {

  private DataSource dataSource;

  @Override
  protected ResourceConfig configure() {
    // Create a datasource to a throwaway postgis database
    dataSource = PostgresUtils.dataSource("jdbc:tc:postgis:13-3.1:///baremaps");

    // Initialize the database
    try (var connection = dataSource.getConnection()) {
      connection.createStatement().execute("create extension if not exists postgis");
      connection.createStatement().execute("create extension if not exists hstore");
      connection.createStatement().execute(
          "create table if not exists features (id integer primary key, property varchar, geometry geometry(Point, 4326))");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Configure the service
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    var style = Paths.get("../examples/openstreetmap/style.json").toAbsolutePath();
    var tileset = Paths.get("../examples/openstreetmap/tileset.json").toAbsolutePath();

    var objectMapper = defaultObjectMapper();

    Tileset config = null;
    try {
      config = objectMapper.readValue(tileset.toFile(), Tileset.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var tileStore = new PostgresTileStore(dataSource, config);

    return new ResourceConfig()
        .registerClasses(
            MultiPartFeature.class,
            DefaultResource.class,
            ApiResource.class,
            ConformanceResource.class,
            CollectionsResource.class,
            StylesResource.class,
            TilesResource.class)
        .register(newContextResolver(objectMapper))
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(tileset).to(Path.class).named("tileset");
            bind(style).to(Path.class).named("style");
            bind(dataSource).to(DataSource.class);
            bind(tileStore).to(TileStore.class);
            bind(objectMapper).to(ObjectMapper.class);
          }
        });
  }

  @Override
  protected void configureClient(ClientConfig clientConfig) {
    clientConfig.register(MultiPartFeature.class);
  }
}
