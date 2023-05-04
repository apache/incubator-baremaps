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

import javax.sql.DataSource;
import org.apache.baremaps.database.PostgresUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgis.PostgisPlugin;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.baremaps.config.DefaultObjectMapper.defaultObjectMapper;

@Tag("integration")
public abstract class OgcApiTest extends JerseyTest {

  private DataSource dataSource;

  private Jdbi jdbi;

  @Override
  protected ResourceConfig configure() {
    // Create a datasource to a throwaway postgis database
    dataSource = PostgresUtils.dataSource("jdbc:tc:postgis:13-3.1:///baremaps");

    // Configure JDBI
    jdbi = Jdbi.create(dataSource)
        .installPlugin(new Jackson2Plugin())
        .installPlugin(new PostgisPlugin());

    // Initialize the database
    jdbi.useHandle(handle -> {
      handle.execute("create extension if not exists hstore");
      handle.execute(
          "create table if not exists features (id integer primary key, property varchar, geometry geometry(Point, 4326))");
    });

    // Configure the service
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);

    var objectMapper = defaultObjectMapper();

    return new ResourceConfig()
        .registerClasses(
            MultiPartFeature.class,
            ApiResource.class,
            CollectionsResource.class,
            ConformanceResource.class,
            DefaultResource.class,
            StylesResource.class,
            TilesResource.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(Paths.get("examples/openstreetmap/style.json")).to(Path.class).named("style");
            bind(Paths.get("examples/openstreetmap/tileset.json")).to(Path.class).named("tileset");
            bind(dataSource).to(DataSource.class);
            bind(jdbi).to(Jdbi.class);
          }
        });
  }

  @Override
  protected void configureClient(ClientConfig clientConfig) {
    clientConfig.register(MultiPartFeature.class);
  }
}
